package com.songs.player.miniplayer

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.songs.player.FakeNowPlayingProvider
import com.songs.player.TestCoroutineRule
import com.songs.player.media.MediaPlayerState
import com.songs.player.presentation.ui.FakeMediaPlayer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MiniPlayerViewModelTest {

    @get:Rule
    val coroutineRule = TestCoroutineRule()

    private lateinit var fakeMediaPlayer: FakeMediaPlayer
    private lateinit var nowPlayingProvider: FakeNowPlayingProvider
    private lateinit var viewModel: MiniPlayerViewModel

    @Before
    fun setUp() {
        fakeMediaPlayer = FakeMediaPlayer()
        nowPlayingProvider = FakeNowPlayingProvider()
        viewModel = MiniPlayerViewModel(fakeMediaPlayer, nowPlayingProvider)
    }

    @Test
    fun `given no media player state, when viewModel initialized, then uiState isVisible is false`() = runTest(coroutineRule.dispatcher) {
        viewModel.uiState.test {
            assertThat(awaitItem().isVisible).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given media player emits trackTitle and artistName, when uiState collected, then uiState becomes visible and maps title and artist`() = runTest(coroutineRule.dispatcher) {
        viewModel.uiState.test {
            skipItems(1) // Skip initial state
            nowPlayingProvider.emit(com.songs.common.playback.NowPlaying(1L, "Song", "Artist", null, emptyList()))
            advanceUntilIdle()

            val state = awaitItem()
            assertThat(state.isVisible).isTrue()
            assertThat(state.trackTitle).isEqualTo("Song")
            assertThat(state.artistName).isEqualTo("Artist")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given media player state with isPlaying true, when uiState collected, then uiState isPlaying is true`() = runTest(coroutineRule.dispatcher) {
        viewModel.uiState.test {
            skipItems(1) // Skip initial state
            fakeMediaPlayer.emitState(MediaPlayerState(isPlaying = true))
            advanceUntilIdle()

            assertThat(awaitItem().isPlaying).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given track route with trackIds and currentTrackId, when updatePlayerRoute invoked and state emitted, then uiState stores trackIds and currentTrackId`() = runTest(coroutineRule.dispatcher) {
        viewModel.uiState.test {
            skipItems(1) // Skip initial state
            nowPlayingProvider.emit(com.songs.common.playback.NowPlaying(2L, "Song", "Artist", null, listOf(1L, 2L, 3L)))
            advanceUntilIdle()

            val state = awaitItem()
            assertThat(state.trackIds).isEqualTo(listOf(1L, 2L, 3L))
            assertThat(state.currentTrackId).isEqualTo(2L)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given viewModel initialized, when togglePlayPause invoked, then mediaPlayer togglePlayPauseCalled is set to true`() = runTest(coroutineRule.dispatcher) {
        viewModel.togglePlayPause()
        assertThat(fakeMediaPlayer.togglePlayPauseCalled).isTrue()
    }
}
