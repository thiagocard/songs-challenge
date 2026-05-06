package com.songs.player.playback

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.songs.common.playback.NowPlaying
import com.songs.player.presentation.ui.FakeMediaPlayer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class MediaPlayerNowPlayingProviderTest {

    @Test
    fun `nowPlaying emits initial track data and subsequent changes`() = runTest {
        val mediaPlayer = FakeMediaPlayer()
        val provider = MediaPlayerNowPlayingProvider(mediaPlayer)

        provider.nowPlaying.test {
            assertThat(awaitItem()).isEqualTo(null)

            mediaPlayer.loadMedia("url", 1L, "title1", "artist1", "art1", listOf(1L))
            assertThat(awaitItem()).isEqualTo(
                NowPlaying(1L, "title1", "artist1", "art1", listOf(1L))
            )

            mediaPlayer.loadMedia("url", 2L, "title2", "artist2", "art2", listOf(1L, 2L))
            assertThat(awaitItem()).isEqualTo(
                NowPlaying(2L, "title2", "artist2", "art2", listOf(1L, 2L))
            )

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `nowPlaying does not re-emit duplicates`() = runTest {
        val mediaPlayer = FakeMediaPlayer()
        val provider = MediaPlayerNowPlayingProvider(mediaPlayer)

        provider.nowPlaying.test {
            assertThat(awaitItem()).isEqualTo(null)

            mediaPlayer.loadMedia("url", 7L, "title7", "artist7", "art7", listOf(7L))
            val expected = NowPlaying(7L, "title7", "artist7", "art7", listOf(7L))
            assertThat(awaitItem()).isEqualTo(expected)

            // Same value again should not emit due to distinctUntilChanged().
            mediaPlayer.loadMedia("url", 7L, "title7", "artist7", "art7", listOf(7L))
            expectNoEvents()

            mediaPlayer.loadMedia("url", 8L, "title8", "artist8", "art8", listOf(8L))
            assertThat(awaitItem()).isEqualTo(
                NowPlaying(8L, "title8", "artist8", "art8", listOf(8L))
            )

            cancelAndIgnoreRemainingEvents()
        }
    }
}
