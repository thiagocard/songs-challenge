package com.songs.home.usecase

import com.songs.home.data.SongsRepository
import com.songs.home.domain.model.AlbumInfo
import com.songs.home.domain.model.AlbumWithSongs
import com.songs.home.domain.usecase.GetSongsByAlbumUseCaseImpl
import com.songs.support.mock.SongMock
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetSongsByAlbumUseCaseImplTest {

    private lateinit var repository: SongsRepository
    private lateinit var useCase: GetSongsByAlbumUseCaseImpl

    private val albumId = "123"

    private val trackSong = SongMock.song.copy(wrapperType = "track")
    private val collectionSong = SongMock.song.copy(
        wrapperType = "collection",
        collectionId = 123L,
        collectionName = "Sample Album",
        artworkUrl100 = "https://example.com/artwork.jpg",
        artistName = "Sample Artist",
    )
    private val albumInfo = AlbumInfo(
        collectionId = 123L,
        title = "Sample Album",
        artistName = "Sample Artist",
        coverUrl = "https://example.com/artwork.jpg",
    )

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        useCase = GetSongsByAlbumUseCaseImpl(repository)
    }

    // --- Cache hit path ---

    @Test
    fun `given cached songs exist, when invoked, then returns AlbumWithSongs from db`() = runTest {
        coEvery { repository.getSongsByAlbumIdFromDb(albumId) } returns listOf(trackSong)
        coEvery { repository.getAlbumFromDb(albumId) } returns albumInfo

        val result = useCase(albumId).first()

        assertEquals(listOf(trackSong), result.songs)
        assertEquals(albumInfo, result.album)
    }

    @Test
    fun `given cached songs exist, when invoked, then does not call remote repository`() = runTest {
        coEvery { repository.getSongsByAlbumIdFromDb(albumId) } returns listOf(trackSong)
        coEvery { repository.getAlbumFromDb(albumId) } returns albumInfo

        useCase(albumId).first()

        coVerify(exactly = 0) { repository.getSongsByAlbumId(any()) }
    }

    @Test
    fun `given cached songs exist but album is not in db, when invoked, then returns empty flow`() = runTest {
        coEvery { repository.getSongsByAlbumIdFromDb(albumId) } returns listOf(trackSong)
        coEvery { repository.getAlbumFromDb(albumId) } returns null

        val result = useCase(albumId).toList()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `given cached songs contain non-track items, when invoked, then filters out non-track songs`() = runTest {
        val nonTrackSong = SongMock.song.copy(wrapperType = "collection")
        coEvery { repository.getSongsByAlbumIdFromDb(albumId) } returns listOf(trackSong, nonTrackSong)
        coEvery { repository.getAlbumFromDb(albumId) } returns albumInfo

        val result = useCase(albumId).first()

        assertEquals(listOf(trackSong), result.songs)
        assertTrue(result.songs.all { it.wrapperType == "track" })
    }

    // --- Cache miss path (remote) ---

    @Test
    fun `given no cached songs, when invoked, then fetches from remote and returns AlbumWithSongs`() = runTest {
        coEvery { repository.getSongsByAlbumIdFromDb(albumId) } returns emptyList()
        coEvery { repository.getSongsByAlbumId(albumId) } returns flowOf(listOf(trackSong, collectionSong))

        val result = useCase(albumId).first()

        assertEquals(listOf(trackSong), result.songs)
        assertEquals(albumInfo, result.album)
    }

    @Test
    fun `given no cached songs, when remote returns data, then saves songs and album to db`() = runTest {
        coEvery { repository.getSongsByAlbumIdFromDb(albumId) } returns emptyList()
        coEvery { repository.getSongsByAlbumId(albumId) } returns flowOf(listOf(trackSong, collectionSong))

        useCase(albumId).first()

        coVerify(exactly = 1) { repository.saveSongsByAlbum(albumId, listOf(trackSong)) }
        coVerify(exactly = 1) { repository.saveAlbum(albumInfo) }
    }

    @Test
    fun `given no cached songs, when remote response has no collection item, then throws error`() = runTest {
        coEvery { repository.getSongsByAlbumIdFromDb(albumId) } returns emptyList()
        coEvery { repository.getSongsByAlbumId(albumId) } returns flowOf(listOf(trackSong)) // no collection

        val exception = runCatching { useCase(albumId).first() }.exceptionOrNull()

        assertTrue(exception is IllegalStateException)
        assertTrue(exception?.message?.contains("Album data not found") == true)
    }

    @Test
    fun `given no cached songs, when remote returns empty songs list, then does not save to db`() = runTest {
        val songsWithNoTracks = listOf(collectionSong) // only collection, no tracks
        coEvery { repository.getSongsByAlbumIdFromDb(albumId) } returns emptyList()
        coEvery { repository.getSongsByAlbumId(albumId) } returns flowOf(songsWithNoTracks)

        useCase(albumId).first()

        coVerify(exactly = 0) { repository.saveSongsByAlbum(any(), any()) }
        coVerify(exactly = 0) { repository.saveAlbum(any()) }
    }

    @Test
    fun `given no cached songs, when remote returns data, then filters non-track songs from result`() = runTest {
        coEvery { repository.getSongsByAlbumIdFromDb(albumId) } returns emptyList()
        coEvery { repository.getSongsByAlbumId(albumId) } returns flowOf(
            listOf(trackSong, collectionSong)
        )

        val result = useCase(albumId).first()

        assertTrue(result.songs.none { it.wrapperType == "collection" })
    }

    @Test
    fun `given no cached songs, when invoked with correct albumId, then calls remote with that albumId`() = runTest {
        coEvery { repository.getSongsByAlbumIdFromDb(albumId) } returns emptyList()
        coEvery { repository.getSongsByAlbumId(albumId) } returns flowOf(listOf(trackSong, collectionSong))

        useCase(albumId)

        coVerify(exactly = 1) { repository.getSongsByAlbumIdFromDb(albumId) }
    }
}
