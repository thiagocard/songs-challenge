package com.songs.player.media

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.google.common.util.concurrent.ListenableFuture
import com.songs.common.coroutine.DispatcherProvider
import com.songs.player.TestCoroutineRule
import io.mockk.CapturingSlot
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class MediaPlayerImplTest {

    @get:Rule
    val coroutineRule = TestCoroutineRule()

    private lateinit var context: Context
    private lateinit var controller: MediaController
    private lateinit var future: ListenableFuture<MediaController>
    private lateinit var listenerSlot: CapturingSlot<Player.Listener>
    private lateinit var mediaItemSlot: CapturingSlot<MediaItem>
    private lateinit var player: MediaPlayerImpl

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        controller = mockk(relaxed = true)
        future = mockk(relaxed = true)
        listenerSlot = slot()
        mediaItemSlot = slot()


        every { controller.currentPosition } returnsMany listOf(10L, 110L, 210L)
        every { controller.duration } returns 999L
        every { controller.addListener(capture(listenerSlot)) } just Runs
        every { controller.removeListener(any()) } just Runs
        every { controller.setMediaItem(capture(mediaItemSlot)) } just Runs
        every { controller.prepare() } just Runs
        every { controller.play() } just Runs
        every { controller.pause() } just Runs
        every { controller.seekTo(any()) } just Runs
        every { controller.release() } just Runs

        mockkConstructor(MediaController.Builder::class)
        every {
            anyConstructed<MediaController.Builder>().buildAsync()
        } returns future
        every { future.get() } returns controller
        every { future.addListener(any(), any()) } answers {
            firstArg<Runnable>().run()
        }

        val dispatcherProvider = object : DispatcherProvider {
            override val main = coroutineRule.dispatcher
            override val io = coroutineRule.dispatcher
        }

        player = MediaPlayerImpl(context, dispatcherProvider)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `loadMedia sets MediaItem metadata and updates state`() {
        player.loadMedia(
            url = "https://example.com/preview.m4a",
            trackId = 42L,
            title = "Track",
            artist = "Artist",
            artworkUri = "https://example.com/art.jpg",
        )

        verify(exactly = 1) { controller.setMediaItem(any()) }
        verify(exactly = 1) { controller.prepare() }

        val mediaItem = mediaItemSlot.captured
        assertThat(mediaItem.localConfiguration?.uri.toString()).isEqualTo("https://example.com/preview.m4a")
        assertThat(mediaItem.mediaMetadata.title.toString()).isEqualTo("Track")
        assertThat(mediaItem.mediaMetadata.artist.toString()).isEqualTo("Artist")
        assertThat(mediaItem.mediaMetadata.artworkUri.toString()).isEqualTo("https://example.com/art.jpg")

        assertThat(player.state.value.trackTitle).isEqualTo("Track")
        assertThat(player.state.value.artistName).isEqualTo("Artist")
        assertThat(player.state.value.artworkUrl).isEqualTo("https://example.com/art.jpg")
        assertThat(player.state.value.currentTrackId).isEqualTo(42L)
    }

    @Test
    fun `play and pause delegate to MediaController`() {
        player.play()
        player.pause()

        verify(exactly = 1) { controller.play() }
        verify(exactly = 1) { controller.pause() }
    }

    @Test
    fun `seekTo delegates to controller and updates current position`() {
        player.seekTo(321L)

        verify(exactly = 1) { controller.seekTo(321L) }
        assertThat(player.state.value.currentPosition).isEqualTo(321L)
    }

    @Test
    fun `togglePlayPause calls play when currently paused and pause when playing`() {
        player.togglePlayPause()
        verify(exactly = 1) { controller.play() }

        listenerSlot.captured.onIsPlayingChanged(true)
        player.togglePlayPause()
        verify(exactly = 1) { controller.pause() }
    }

    @Test
    fun `player callbacks update state and emit playback errors`() =
        runTest(coroutineRule.dispatcher) {
            player.playbackErrors.test {
                @Suppress("DEPRECATION")
                val oldPosition = Player.PositionInfo(null, 0, null, 0, 0L, 0L, 0, 0)
                @Suppress("DEPRECATION")
                val newPosition = Player.PositionInfo(null, 0, null, 0, 555L, 0L, 0, 0)
                listenerSlot.captured.onPositionDiscontinuity(
                    oldPosition,
                    newPosition,
                    Player.DISCONTINUITY_REASON_SEEK,
                )
                assertThat(player.state.value.currentPosition).isEqualTo(555L)

                listenerSlot.captured.onPlaybackStateChanged(Player.STATE_READY)
                assertThat(player.state.value.duration).isEqualTo(999L)

                listenerSlot.captured.onPlayerError(mockk<PlaybackException>(relaxed = true))
                assertThat(awaitItem()).isEqualTo(Unit)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `isPlaying callback starts and stops periodic position updates`() =
        runTest(coroutineRule.dispatcher) {
            assertThat(player.state.value.isPlaying).isFalse()

            listenerSlot.captured.onIsPlayingChanged(true)
            runCurrent()
            assertThat(player.state.value.isPlaying).isTrue()

            advanceTimeBy(110)
            assertThat(player.state.value.currentPosition).isEqualTo(110L)

            listenerSlot.captured.onIsPlayingChanged(false)
            val positionAfterStop = player.state.value.currentPosition
            advanceTimeBy(250)
            assertThat(player.state.value.isPlaying).isFalse()
            assertThat(player.state.value.currentPosition).isEqualTo(positionAfterStop)
        }

    @Test
    fun `release removes listener and releases controller`() {
        player.release()

        verify(exactly = 1) { controller.removeListener(listenerSlot.captured) }
        verify(exactly = 1) { controller.release() }
    }
}
