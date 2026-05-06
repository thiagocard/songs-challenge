package com.songs.home.songs

import androidx.paging.PagingData
import com.songs.common.playback.NowPlayingProvider
import com.songs.home.TestCoroutineRule
import com.songs.home.domain.usecase.GetSongsUseCase
import com.songs.home.presentation.ui.songs.SongsViewModel
import com.songs.support.mock.SongMock
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SongsViewModelTest {

    private lateinit var songsViewModel: SongsViewModel
    private lateinit var getSongsUseCase: GetSongsUseCase
    private lateinit var nowPlayingProvider: NowPlayingProvider

    @get:Rule
    var executorRule = TestCoroutineRule()

    private val mockSongs = SongMock.songList

    @Before
    fun setUp() {
        getSongsUseCase = mockk()
        nowPlayingProvider = mockk()
        every { nowPlayingProvider.nowPlaying } returns flowOf(null)
        every { getSongsUseCase(any()) } returns flowOf(PagingData.from(mockSongs))

        songsViewModel = SongsViewModel(
            getSongsUseCase = getSongsUseCase,
            nowPlayingProvider = nowPlayingProvider,
        )
    }

    @Test
    fun `searchTerm initial value is empty`() {
        assertThat(songsViewModel.uiState.value.searchTerm).isEqualTo("")
    }

    @Test
    fun `should update searchTerm when onSearchTermChanged is called`() = runTest {
        val uiStateJob = backgroundScope.launch { songsViewModel.uiState.collect() }
        songsViewModel.onSearchTermChanged("jazz")
        advanceUntilIdle()
        assertThat(songsViewModel.uiState.value.searchTerm).isEqualTo("jazz")
        uiStateJob.cancel()
    }

    @Test
    fun `should debounce rapid search term changes and invoke use case only for final term`() = runTest {
        // Collect to activate the paging flow; otherwise the use case is never invoked.
        val uiStateJob = backgroundScope.launch { songsViewModel.uiState.collect() }
        val collectJob = backgroundScope.launch {
            songsViewModel.pagingData.collect()
        }

        songsViewModel.onSearchTermChanged("r")
        advanceTimeBy(100L)
        songsViewModel.onSearchTermChanged("ro")
        advanceTimeBy(100L)
        songsViewModel.onSearchTermChanged("roc")
        advanceTimeBy(100L)
        songsViewModel.onSearchTermChanged("rock")
        advanceTimeBy(350L)
        advanceUntilIdle()

        assertThat(songsViewModel.uiState.value.searchTerm).isEqualTo("rock")
        verify(exactly = 0) { getSongsUseCase("r") }
        verify(exactly = 0) { getSongsUseCase("ro") }
        verify(exactly = 0) { getSongsUseCase("roc") }
        verify(exactly = 1) { getSongsUseCase("rock") }

        collectJob.cancel()
        uiStateJob.cancel()
    }

    @Test
    fun `should handle multiple search term changes sequentially`() = runTest {
        val uiStateJob = backgroundScope.launch { songsViewModel.uiState.collect() }
        every { getSongsUseCase("jazz") } returns flowOf(PagingData.from(mockSongs.take(1)))
        every { getSongsUseCase("rock") } returns flowOf(PagingData.from(mockSongs.drop(1)))

        songsViewModel.onSearchTermChanged("jazz")
        advanceUntilIdle()
        assertThat(songsViewModel.uiState.value.searchTerm).isEqualTo("jazz")

        songsViewModel.onSearchTermChanged("rock")
        advanceUntilIdle()
        assertThat(songsViewModel.uiState.value.searchTerm).isEqualTo("rock")
        uiStateJob.cancel()
    }

    @Test
    fun `resetToDefault clears the search term`() = runTest {
        val uiStateJob = backgroundScope.launch { songsViewModel.uiState.collect() }
        every { getSongsUseCase("jazz") } returns flowOf(PagingData.from(mockSongs))

        songsViewModel.onSearchTermChanged("jazz")
        advanceUntilIdle()

        songsViewModel.resetToDefault()
        advanceUntilIdle()

        assertThat(songsViewModel.uiState.value.searchTerm).isEqualTo("")
        uiStateJob.cancel()
    }

    @Test
    fun `blank search term is stored as-is in state`() = runTest {
        val uiStateJob = backgroundScope.launch { songsViewModel.uiState.collect() }
        songsViewModel.onSearchTermChanged("   ")
        advanceUntilIdle()

        // The raw search term is stored as entered; the mapping to DEFAULT_TERM ("rock") happens
        // internally in the pagingData flow when the term is blank.
        assertThat(songsViewModel.uiState.value.searchTerm).isEqualTo("   ")
        uiStateJob.cancel()
    }
}
