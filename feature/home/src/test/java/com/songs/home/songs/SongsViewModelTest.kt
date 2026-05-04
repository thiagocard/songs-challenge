package com.songs.home.songs

import androidx.paging.PagingData
import com.songs.home.TestCoroutineRule
import com.songs.home.domain.usecase.GetSongsUseCase
import com.songs.home.presentation.ui.songs.SongsViewModel
import com.songs.support.mock.SongMock
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    @get:Rule
    var executorRule = TestCoroutineRule()

    private val mockSongs = SongMock.songList

    @Before
    fun setUp() {
        getSongsUseCase = mockk()
        every { getSongsUseCase(any()) } returns flowOf(PagingData.from(mockSongs))

        songsViewModel = SongsViewModel(getSongsUseCase = getSongsUseCase)
    }

    @Test
    fun `searchTerm initial value is empty`() {
        assertThat(songsViewModel.searchTerm.value).isEqualTo("")
    }

    @Test
    fun `should update searchTerm when onSearchTermChanged is called`() = runTest {
        songsViewModel.onSearchTermChanged("jazz")
        advanceTimeBy(350L)
        advanceUntilIdle()
        assertThat(songsViewModel.searchTerm.value).isEqualTo("jazz")
    }

    @Test
    fun `should debounce rapid search term changes and invoke use case only for final term`() = runTest {
        every { getSongsUseCase("rock") } returns flowOf(PagingData.from(mockSongs))

        songsViewModel.onSearchTermChanged("r")
        advanceTimeBy(100L)
        songsViewModel.onSearchTermChanged("ro")
        advanceTimeBy(100L)
        songsViewModel.onSearchTermChanged("roc")
        advanceTimeBy(100L)
        songsViewModel.onSearchTermChanged("rock")
        advanceTimeBy(350L)
        advanceUntilIdle()

        assertThat(songsViewModel.searchTerm.value).isEqualTo("rock")
        // Only "pop" (initial) and "rock" should have been queried; intermediate terms should not.
        verify(exactly = 0) { getSongsUseCase("r") }
        verify(exactly = 0) { getSongsUseCase("ro") }
        verify(exactly = 0) { getSongsUseCase("roc") }
    }

    @Test
    fun `should handle multiple search term changes sequentially`() = runTest {
        every { getSongsUseCase("jazz") } returns flowOf(PagingData.from(mockSongs.take(1)))
        every { getSongsUseCase("rock") } returns flowOf(PagingData.from(mockSongs.drop(1)))

        songsViewModel.onSearchTermChanged("jazz")
        advanceTimeBy(350L)
        advanceUntilIdle()
        assertThat(songsViewModel.searchTerm.value).isEqualTo("jazz")

        songsViewModel.onSearchTermChanged("rock")
        advanceTimeBy(350L)
        advanceUntilIdle()
        assertThat(songsViewModel.searchTerm.value).isEqualTo("rock")
    }

    @Test
    fun `resetToDefault clears the search term`() = runTest {
        every { getSongsUseCase("jazz") } returns flowOf(PagingData.from(mockSongs))

        songsViewModel.onSearchTermChanged("jazz")
        advanceTimeBy(350L)
        advanceUntilIdle()

        songsViewModel.resetToDefault()
        advanceTimeBy(350L)
        advanceUntilIdle()

        assertThat(songsViewModel.searchTerm.value).isEqualTo("")
    }

    @Test
    fun `blank search term is stored as-is in state`() = runTest {
        songsViewModel.onSearchTermChanged("   ")
        advanceTimeBy(350L)
        advanceUntilIdle()

        // The raw search term is stored as entered; the mapping to DEFAULT_TERM ("pop") happens
        // internally in the pagingData flow when the term is blank.
        assertThat(songsViewModel.searchTerm.value).isEqualTo("   ")
    }
}
