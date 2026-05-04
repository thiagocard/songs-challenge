package com.songs.home.usecase

import androidx.paging.PagingData
import com.songs.home.data.SongsRepository
import com.songs.home.domain.model.Song
import com.songs.home.domain.usecase.GetSongsUseCaseImpl
import com.songs.support.mock.SongMock
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetSongsUseCaseImplTest {

    private lateinit var repository: SongsRepository
    private lateinit var useCase: GetSongsUseCaseImpl

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetSongsUseCaseImpl(repository)
    }

    @Test
    fun `when invoked with a term, then delegates to repository with that term`() = runTest {
        val term = "rock"
        val expected = flowOf(PagingData.from(SongMock.songList))
        every { repository.getSongsPagingFlow(term) } returns expected

        val result = useCase(term)

        assertEquals(expected, result)
        verify(exactly = 1) { repository.getSongsPagingFlow(term) }
    }

    @Test
    fun `when invoked with default term, then delegates to repository with pop`() = runTest {
        val expected = flowOf(PagingData.from(SongMock.songList))
        every { repository.getSongsPagingFlow("pop") } returns expected

        val result = useCase()

        assertEquals(expected, result)
        verify(exactly = 1) { repository.getSongsPagingFlow("pop") }
    }

    @Test
    fun `when invoked with empty term, then delegates to repository with empty string`() = runTest {
        val expected = flowOf(PagingData.empty<Song>())
        every { repository.getSongsPagingFlow("") } returns expected

        val result = useCase("")

        assertEquals(expected, result)
        verify(exactly = 1) { repository.getSongsPagingFlow("") }
    }

    @Test
    fun `when invoked multiple times with different terms, then each call delegates independently`() = runTest {
        every { repository.getSongsPagingFlow(any()) } returns flowOf(PagingData.empty())

        useCase("jazz")
        useCase("pop")
        useCase("rock")

        verify(exactly = 1) { repository.getSongsPagingFlow("jazz") }
        verify(exactly = 1) { repository.getSongsPagingFlow("pop") }
        verify(exactly = 1) { repository.getSongsPagingFlow("rock") }
    }
}
