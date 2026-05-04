package com.songs.player.usecase

import app.cash.turbine.test
import com.songs.player.data.TrackRepository
import com.songs.player.domain.usecase.GetTrackByIdUseCaseImpl
import com.songs.support.mock.TrackMock
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetTrackByIdUseCaseImplTest {

    private lateinit var repository: TrackRepository
    private lateinit var useCase: GetTrackByIdUseCaseImpl

    private val track = TrackMock.track
    private val trackId = track.trackId!!          // 1L
    private val trackIdStr = trackId.toString()    // "1"

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        useCase = GetTrackByIdUseCaseImpl(repository)
    }

    // -----------------------------------------------------------------------
    // Path 1 – tracks table cache hit
    // -----------------------------------------------------------------------

    @Test
    fun `given track exists in tracks db, when invoked, then emits cached track and stops`() = runTest {
        coEvery { repository.getTrackFromDb(trackId) } returns listOf(track)

        useCase(trackIdStr).test {
            assertEquals(listOf(track), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `given track exists in tracks db, when invoked, then does not call remote`() = runTest {
        coEvery { repository.getTrackFromDb(trackId) } returns listOf(track)

        useCase(trackIdStr).test {
            awaitItem()
            awaitComplete()
        }

        coVerify(exactly = 0) { repository.getTrack(any()) }
    }

    @Test
    fun `given track exists in tracks db, when invoked, then does not check songs db`() = runTest {
        coEvery { repository.getTrackFromDb(trackId) } returns listOf(track)

        useCase(trackIdStr).test {
            awaitItem()
            awaitComplete()
        }

        coVerify(exactly = 0) { repository.getTrackFromSongsDb(any()) }
    }

    // -----------------------------------------------------------------------
    // Path 2 – songs table cache hit (tracks table empty)
    // -----------------------------------------------------------------------

    @Test
    fun `given track only in songs db, when invoked, then first emission is cached song`() = runTest {
        coEvery { repository.getTrackFromDb(trackId) } returns emptyList()
        coEvery { repository.getTrackFromSongsDb(trackId) } returns listOf(track)
        coEvery { repository.getTrack(trackIdStr) } returns flowOf(listOf(track))

        useCase(trackIdStr).test {
            assertEquals(listOf(track), awaitItem()) // cached song
            awaitItem()                              // network result
            awaitComplete()
        }
    }

    @Test
    fun `given track only in songs db, when network succeeds, then also emits remote tracks`() = runTest {
        val networkTrack = track.copy(trackName = "Updated Name")
        coEvery { repository.getTrackFromDb(trackId) } returns emptyList()
        coEvery { repository.getTrackFromSongsDb(trackId) } returns listOf(track)
        coEvery { repository.getTrack(trackIdStr) } returns flowOf(listOf(networkTrack))

        useCase(trackIdStr).test {
            awaitItem()                                              // cached song
            assertEquals(listOf(networkTrack), awaitItem())         // network result
            awaitComplete()
        }
    }

    @Test
    fun `given track only in songs db, when network succeeds, then saves tracks to db`() = runTest {
        val networkTrack = track.copy(trackName = "Updated Name")
        coEvery { repository.getTrackFromDb(trackId) } returns emptyList()
        coEvery { repository.getTrackFromSongsDb(trackId) } returns listOf(track)
        coEvery { repository.getTrack(trackIdStr) } returns flowOf(listOf(networkTrack))

        useCase(trackIdStr).test { cancelAndIgnoreRemainingEvents() }

        coVerify(exactly = 1) { repository.saveTracks(listOf(networkTrack)) }
    }

    @Test
    fun `given track only in songs db, when network fails, then error is swallowed`() = runTest {
        coEvery { repository.getTrackFromDb(trackId) } returns emptyList()
        coEvery { repository.getTrackFromSongsDb(trackId) } returns listOf(track)
        coEvery { repository.getTrack(trackIdStr) } throws RuntimeException("Network error")

        useCase(trackIdStr).test {
            assertEquals(listOf(track), awaitItem())
            awaitComplete() // no error propagated because cached data was emitted
        }
    }

    // -----------------------------------------------------------------------
    // Path 3 – both caches empty (network only)
    // -----------------------------------------------------------------------

    @Test
    fun `given both caches empty, when network succeeds, then emits network tracks`() = runTest {
        coEvery { repository.getTrackFromDb(trackId) } returns emptyList()
        coEvery { repository.getTrackFromSongsDb(trackId) } returns emptyList()
        coEvery { repository.getTrack(trackIdStr) } returns flowOf(listOf(track))

        useCase(trackIdStr).test {
            assertEquals(listOf(track), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `given both caches empty, when network succeeds, then saves tracks to db`() = runTest {
        coEvery { repository.getTrackFromDb(trackId) } returns emptyList()
        coEvery { repository.getTrackFromSongsDb(trackId) } returns emptyList()
        coEvery { repository.getTrack(trackIdStr) } returns flowOf(listOf(track))

        useCase(trackIdStr).test { cancelAndIgnoreRemainingEvents() }

        coVerify(exactly = 1) { repository.saveTracks(listOf(track)) }
    }

    @Test
    fun `given both caches empty, when network fails, then propagates exception`() = runTest {
        val error = RuntimeException("Network error")
        coEvery { repository.getTrackFromDb(trackId) } returns emptyList()
        coEvery { repository.getTrackFromSongsDb(trackId) } returns emptyList()
        coEvery { repository.getTrack(trackIdStr) } throws error

        useCase(trackIdStr).test {
            val thrown = awaitError()
            assertEquals(error.message, thrown.message)
        }
    }

    @Test
    fun `given both caches empty, when network returns empty list, then no emission occurs`() = runTest {
        coEvery { repository.getTrackFromDb(trackId) } returns emptyList()
        coEvery { repository.getTrackFromSongsDb(trackId) } returns emptyList()
        coEvery { repository.getTrack(trackIdStr) } returns flowOf(emptyList())

        useCase(trackIdStr).test {
            awaitComplete()
        }
    }

    // -----------------------------------------------------------------------
    // Edge cases
    // -----------------------------------------------------------------------

    @Test
    fun `given invalid trackId string, when invoked, then uses 0L as fallback id`() = runTest {
        coEvery { repository.getTrackFromDb(0L) } returns listOf(track)

        useCase("not-a-number").test {
            assertEquals(listOf(track), awaitItem())
            awaitComplete()
        }

        coVerify(exactly = 1) { repository.getTrackFromDb(0L) }
    }
}
