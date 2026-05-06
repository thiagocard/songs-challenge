package com.songs.home.songs

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import androidx.paging.PagingData
import com.songs.common.playback.NowPlayingProvider
import com.songs.home.TestCoroutineRule
import com.songs.home.domain.usecase.GetSongsUseCase
import com.songs.home.presentation.ui.songs.SongsSideEffect
import com.songs.home.presentation.ui.songs.SongsViewModel
import com.songs.support.mock.SongMock
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Tests for [SongsViewModel.onSongClick] via the internal testable overload that accepts
 * a plain [List<Song?>] snapshot instead of [LazyPagingItems]. This avoids the need for
 * a Compose runtime in pure unit tests.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SongsViewModelOnSongClickTest {

    @get:Rule
    var coroutineRule = TestCoroutineRule()

    private lateinit var getSongsUseCase: GetSongsUseCase
    private lateinit var nowPlayingProvider: NowPlayingProvider
    private lateinit var viewModel: SongsViewModel

    private val song1 = SongMock.song.copy(trackId = 1L, trackName = "First Song")
    private val song2 = SongMock.song.copy(trackId = 2L, trackName = "Second Song")
    private val song3 = SongMock.song.copy(trackId = 3L, trackName = "Third Song")

    @Before
    fun setUp() {
        getSongsUseCase = mockk()
        nowPlayingProvider = mockk()
        every { nowPlayingProvider.nowPlaying } returns flowOf(null)
        every { getSongsUseCase(any()) } returns flowOf(PagingData.from(listOf(song1, song2, song3)))

        viewModel = SongsViewModel(
            getSongsUseCase = getSongsUseCase,
            nowPlayingProvider = nowPlayingProvider,
        )
    }

    /** Convenience to call the internal testable overload. */
    private fun SongsViewModel.onSongClickTest(
        song: com.songs.home.domain.model.Song,
        items: List<com.songs.home.domain.model.Song>,
    ) = onSongClick(
        song = song,
        snapshot = items,
        itemCount = items.size,
        peek = { index -> items.getOrNull(index) },
    )

    @Test
    fun `onSongClick with valid trackId emits NavigateToPlayer with correct currentTrackId`() =
        runTest(coroutineRule.dispatcher) {
            viewModel.events.test {
                viewModel.onSongClickTest(song2, listOf(song1, song2, song3))
                advanceUntilIdle()

                val event = awaitItem() as SongsSideEffect.NavigateToPlayer
                assertThat(event.currentTrackId).isEqualTo(2L)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `onSongClick with valid trackId emits NavigateToPlayer with all visible trackIds`() =
        runTest(coroutineRule.dispatcher) {
            viewModel.events.test {
                viewModel.onSongClickTest(song1, listOf(song1, song2, song3))
                advanceUntilIdle()

                val event = awaitItem() as SongsSideEffect.NavigateToPlayer
                assertThat(event.trackIds).isEqualTo(listOf(1L, 2L, 3L))
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `onSongClick with null trackId emits no event`() =
        runTest(coroutineRule.dispatcher) {
            val songWithNullId = song1.copy(trackId = null)

            viewModel.events.test {
                viewModel.onSongClickTest(songWithNullId, listOf(songWithNullId, song2))
                advanceUntilIdle()

                expectNoEvents()
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `onSongClick when song not in snapshot emits no event`() =
        runTest(coroutineRule.dispatcher) {
            // Paging snapshot does NOT include song3
            viewModel.events.test {
                viewModel.onSongClickTest(song3, listOf(song1, song2))
                advanceUntilIdle()

                expectNoEvents()
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `onSongClick with single song in snapshot uses that song as only trackId`() =
        runTest(coroutineRule.dispatcher) {
            viewModel.events.test {
                viewModel.onSongClickTest(song1, listOf(song1))
                advanceUntilIdle()

                val event = awaitItem() as SongsSideEffect.NavigateToPlayer
                assertThat(event.trackIds).isEqualTo(listOf(1L))
                assertThat(event.currentTrackId).isEqualTo(1L)
                cancelAndIgnoreRemainingEvents()
            }
        }
}
