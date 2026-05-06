package com.songs.player.presentation.ui

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import assertk.assertions.isEqualTo
import com.songs.player.FakeNowPlayingProvider
import com.songs.player.TestCoroutineRule
import com.songs.player.domain.usecase.GetTrackByIdUseCase
import com.songs.player.media.MediaPlayerState
import com.songs.support.mock.TrackMock
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModelTest {

    @get:Rule
    val coroutineRule = TestCoroutineRule()

    private lateinit var getTrackByIdUseCase: GetTrackByIdUseCase
    private lateinit var fakeMediaPlayer: FakeMediaPlayer
    private lateinit var viewModel: PlayerViewModel
    private lateinit var nowPlayingProvider: FakeNowPlayingProvider

    private val track1 = TrackMock.track.copy(trackId = 1L, trackName = "Track 1", previewUrl = "https://preview1.url")
    private val track2 = TrackMock.track.copy(trackId = 2L, trackName = "Track 2", previewUrl = "https://preview2.url")
    private val track3 = TrackMock.track.copy(trackId = 3L, trackName = "Track 3", previewUrl = "https://preview3.url")

    @Before
    fun setUp() {
        getTrackByIdUseCase = mockk()
        fakeMediaPlayer = FakeMediaPlayer()
        nowPlayingProvider = FakeNowPlayingProvider()
        coEvery { getTrackByIdUseCase(any()) } returns flowOf(emptyList())
    }

    private fun createViewModel() =
        PlayerViewModel(getTrackByIdUseCase, fakeMediaPlayer, nowPlayingProvider)

    @Test
    fun `loadTrack with trackId=0 sets PlaylistUiState Error`() = runTest(coroutineRule.dispatcher) {
        viewModel = createViewModel()
        viewModel.loadTrack(0L, emptyList())
        advanceUntilIdle()

        viewModel.playlistUiState.test {
            assertThat(awaitItem()).isInstanceOf(PlaylistUiState.Error::class)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadTrack without requestPlay skips playTrack (back-navigation)`() = runTest(coroutineRule.dispatcher) {
        coEvery { getTrackByIdUseCase("1") } returns flowOf(listOf(track1))
        viewModel = createViewModel()

        // No requestPlay called — simulates back-navigation
        viewModel.loadTrack(1L, listOf(1L))
        advanceUntilIdle()

        assertThat(fakeMediaPlayer.loadMediaCalled).isFalse()
    }

    @Test
    fun `requestPlay then loadTrack triggers playTrack`() = runTest(coroutineRule.dispatcher) {
        coEvery { getTrackByIdUseCase("1") } returns flowOf(listOf(track1))
        viewModel = createViewModel()

        viewModel.requestPlay(1L)
        viewModel.loadTrack(1L, listOf(1L))
        advanceUntilIdle()

        assertThat(fakeMediaPlayer.loadMediaCalled).isTrue()
        assertThat(fakeMediaPlayer.lastLoadedUrl).isEqualTo(track1.previewUrl)
    }

    @Test
    fun `loadTrack with single track emits Success with current track playlist`() = runTest(coroutineRule.dispatcher) {
        coEvery { getTrackByIdUseCase("1") } returns flowOf(listOf(track1))
        viewModel = createViewModel()

        viewModel.requestPlay(1L)
        viewModel.loadTrack(1L, listOf(1L))
        advanceUntilIdle()

        viewModel.playlistUiState.test {
            val state = awaitItem()
            assertThat(state).isInstanceOf(PlaylistUiState.Success::class)
            assertThat((state as PlaylistUiState.Success).playableTracks).hasSize(1)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadTrack with multiple tracks emits Success with all tracks`() = runTest(coroutineRule.dispatcher) {
        coEvery { getTrackByIdUseCase("1") } returns flowOf(listOf(track1))
        coEvery { getTrackByIdUseCase("2") } returns flowOf(listOf(track2))
        coEvery { getTrackByIdUseCase("3") } returns flowOf(listOf(track3))
        viewModel = createViewModel()

        viewModel.requestPlay(1L)
        viewModel.loadTrack(1L, listOf(1L, 2L, 3L))
        advanceUntilIdle()

        viewModel.playlistUiState.test {
            val state = awaitItem()
            assertThat(state).isInstanceOf(PlaylistUiState.Success::class)
            assertThat((state as PlaylistUiState.Success).playableTracks).hasSize(3)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadTrack emits Error when use case throws`() = runTest(coroutineRule.dispatcher) {
        coEvery { getTrackByIdUseCase("1") } throws Exception("Network error")
        viewModel = createViewModel()

        viewModel.requestPlay(1L)
        viewModel.loadTrack(1L, listOf(1L))
        advanceUntilIdle()

        viewModel.playlistUiState.test {
            assertThat(awaitItem()).isInstanceOf(PlaylistUiState.Error::class)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `nextTrack advances to next song in playlist`() = runTest(coroutineRule.dispatcher) {
        coEvery { getTrackByIdUseCase("1") } returns flowOf(listOf(track1))
        coEvery { getTrackByIdUseCase("2") } returns flowOf(listOf(track2))
        viewModel = createViewModel()

        viewModel.requestPlay(1L)
        viewModel.loadTrack(1L, listOf(1L, 2L))
        advanceUntilIdle()

        fakeMediaPlayer.resetCalls()
        viewModel.nextTrack()

        assertThat(fakeMediaPlayer.loadMediaCalled).isTrue()
        assertThat(fakeMediaPlayer.lastLoadedUrl).isEqualTo(track2.previewUrl)
    }

    @Test
    fun `nextTrack does nothing when already at last song`() = runTest(coroutineRule.dispatcher) {
        coEvery { getTrackByIdUseCase("2") } returns flowOf(listOf(track2))
        viewModel = createViewModel()

        viewModel.requestPlay(2L)
        viewModel.loadTrack(2L, listOf(2L))
        advanceUntilIdle()

        fakeMediaPlayer.resetCalls()
        viewModel.nextTrack()

        assertThat(fakeMediaPlayer.loadMediaCalled).isFalse()
    }

    @Test
    fun `previousTrack goes back one song`() = runTest(coroutineRule.dispatcher) {
        coEvery { getTrackByIdUseCase("1") } returns flowOf(listOf(track1))
        coEvery { getTrackByIdUseCase("2") } returns flowOf(listOf(track2))
        viewModel = createViewModel()

        viewModel.requestPlay(2L)
        viewModel.loadTrack(2L, listOf(1L, 2L))
        advanceUntilIdle()

        fakeMediaPlayer.resetCalls()
        viewModel.previousTrack()

        assertThat(fakeMediaPlayer.loadMediaCalled).isTrue()
        assertThat(fakeMediaPlayer.lastLoadedUrl).isEqualTo(track1.previewUrl)
    }

    @Test
    fun `previousTrack does nothing when at first song`() = runTest(coroutineRule.dispatcher) {
        coEvery { getTrackByIdUseCase("1") } returns flowOf(listOf(track1))
        viewModel = createViewModel()

        viewModel.requestPlay(1L)
        viewModel.loadTrack(1L, listOf(1L))
        advanceUntilIdle()

        fakeMediaPlayer.resetCalls()
        viewModel.previousTrack()

        assertThat(fakeMediaPlayer.loadMediaCalled).isFalse()
    }

    @Test
    fun `toggleRepeat flips isRepeatOn flag`() = runTest(coroutineRule.dispatcher) {
        viewModel = createViewModel()

        viewModel.uiState.test {
            // skip initial Loading or Success state emitted by observeMediaPlayer
            skipItems(1)

            viewModel.toggleRepeat()
            val afterToggle = awaitItem()
            assertThat((afterToggle as PlayerUiState.Success).isRepeatOn).isTrue()

            viewModel.toggleRepeat()
            val afterSecondToggle = awaitItem()
            assertThat((afterSecondToggle as PlayerUiState.Success).isRepeatOn).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeMediaPlayer updates sliderPosition and formattedCurrentPosition`() = runTest(coroutineRule.dispatcher) {
        viewModel = createViewModel()

        fakeMediaPlayer.emitState(
            MediaPlayerState(
                isPlaying = true,
                currentPosition = 30_000L,
                duration = 60_000L,
            )
        )
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem() as PlayerUiState.Success
            assertThat(state.isPlaying).isTrue()
            assertThat(state.sliderPosition).isEqualTo(0.5f)
            assertThat(state.formattedCurrentPosition).isEqualTo("00:30")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `track near end triggers nextTrack when repeat is off`() = runTest(coroutineRule.dispatcher) {
        coEvery { getTrackByIdUseCase("1") } returns flowOf(listOf(track1))
        coEvery { getTrackByIdUseCase("2") } returns flowOf(listOf(track2))
        viewModel = createViewModel()

        viewModel.requestPlay(1L)
        viewModel.loadTrack(1L, listOf(1L, 2L))
        advanceUntilIdle()

        fakeMediaPlayer.resetCalls()

        // Simulate track near end (within last 500ms of a 5s track)
        fakeMediaPlayer.emitState(
            MediaPlayerState(isPlaying = true, currentPosition = 4_600L, duration = 5_000L)
        )
        advanceUntilIdle()

        assertThat(fakeMediaPlayer.loadMediaCalled).isTrue()
        assertThat(fakeMediaPlayer.lastLoadedUrl).isEqualTo(track2.previewUrl)
    }

    @Test
    fun `track near end triggers replayCurrentTrack when repeat is on`() = runTest(coroutineRule.dispatcher) {
        coEvery { getTrackByIdUseCase("1") } returns flowOf(listOf(track1))
        viewModel = createViewModel()

        viewModel.requestPlay(1L)
        viewModel.loadTrack(1L, listOf(1L))
        advanceUntilIdle()

        viewModel.toggleRepeat()

        fakeMediaPlayer.resetCalls()

        // Simulate track near end
        fakeMediaPlayer.emitState(
            MediaPlayerState(isPlaying = true, currentPosition = 4_600L, duration = 5_000L)
        )
        advanceUntilIdle()

        // Replay should reload the same track
        assertThat(fakeMediaPlayer.loadMediaCalled).isTrue()
        assertThat(fakeMediaPlayer.lastLoadedUrl).isEqualTo(track1.previewUrl)
    }
}
