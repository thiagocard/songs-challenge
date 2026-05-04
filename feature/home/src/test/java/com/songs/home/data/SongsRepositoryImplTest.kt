package com.songs.home.data

import com.songs.database.SongsDatabase
import com.songs.database.dao.AlbumDao
import com.songs.database.dao.SongDao
import com.songs.database.entity.AlbumEntity
import com.songs.database.entity.SongEntity
import com.songs.home.data.model.ListSongsResponse
import com.songs.home.data.model.SongResponse
import com.songs.home.data.remote.SongsRemoteDataSource
import com.songs.home.domain.mapper.SongsMapper
import com.songs.home.domain.model.AlbumInfo
import com.songs.home.domain.model.Song
import com.songs.networking.NetworkResponse
import com.songs.support.mock.SongMock
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import org.junit.Before
import org.junit.Test

class SongsRepositoryImplTest {

    private lateinit var remoteDataSource: SongsRemoteDataSource
    private lateinit var songsMapper: SongsMapper
    private lateinit var songDao: SongDao
    private lateinit var albumDao: AlbumDao
    private lateinit var database: SongsDatabase
    private lateinit var repository: SongsRepositoryImpl

    private val song = SongMock.song
    private val albumId = song.collectionId.toString()

    private val songEntity = SongEntity(
        trackId = song.trackId ?: 0L,
        searchTerm = albumId,
        albumId = song.collectionId,
        wrapperType = song.wrapperType,
        kind = song.kind,
        collectionId = song.collectionId,
        artistName = song.artistName,
        collectionName = song.collectionName,
        trackName = song.trackName,
        collectionCensoredName = song.collectionCensoredName,
        trackCensoredName = song.trackCensoredName,
        collectionArtistId = song.collectionArtistId,
        collectionArtistViewUrl = song.collectionArtistViewUrl,
        collectionViewUrl = song.collectionViewUrl,
        trackViewUrl = song.trackViewUrl,
        previewUrl = song.previewUrl,
        artworkUrl30 = song.artworkUrl30,
        artworkUrl60 = song.artworkUrl60,
        artworkUrl100 = song.artworkUrl100,
        collectionPrice = song.collectionPrice,
        trackPrice = song.trackPrice,
        trackRentalPrice = song.trackRentalPrice,
        collectionHdPrice = song.collectionHdPrice,
        trackHdPrice = song.trackHdPrice,
        trackHdRentalPrice = song.trackHdRentalPrice,
        releaseDate = song.releaseDate,
        collectionExplicitness = song.collectionExplicitness,
        trackExplicitness = song.trackExplicitness,
        trackCount = song.trackCount,
        trackNumber = song.trackNumber,
        trackTimeMillis = song.trackTimeMillis,
        country = song.country,
        currency = song.currency,
        primaryGenreName = song.primaryGenreName,
        contentAdvisoryRating = song.contentAdvisoryRating,
        shortDescription = song.shortDescription,
        longDescription = song.longDescription,
        hasITunesExtras = song.hasITunesExtras,
    )

    private val albumInfo = AlbumInfo(
        collectionId = 123L,
        title = "Sample Album",
        artistName = "Sample Artist",
        coverUrl = "https://example.com/artwork.jpg",
    )

    private val albumEntity = AlbumEntity(
        collectionId = albumInfo.collectionId,
        title = albumInfo.title,
        artistName = albumInfo.artistName,
        coverUrl = albumInfo.coverUrl,
    )

    private val listSongsResponse = ListSongsResponse(
        resultCount = 1,
        results = listOf(SongResponse(trackId = song.trackId, artistName = song.artistName))
    )

    @Before
    fun setUp() {
        remoteDataSource = mockk()
        songsMapper = mockk()
        songDao = mockk(relaxed = true)
        albumDao = mockk(relaxed = true)
        database = mockk(relaxed = true)
        repository = SongsRepositoryImpl(remoteDataSource, songsMapper, songDao, albumDao, database)
    }

    // -----------------------------------------------------------------------
    // getSongsByAlbumId
    // -----------------------------------------------------------------------

    @Test
    fun `getSongsByAlbumId - returns mapped songs from remote`() = runTest {
        coEvery { remoteDataSource.getSongsByCollectionId(albumId) } returns
                NetworkResponse.Success(listSongsResponse)
        coEvery { songsMapper.map(listSongsResponse) } returns listOf(song)

        val result = repository.getSongsByAlbumId(albumId).first()

        assertThat(result).isEqualTo(listOf(song))
    }

    @Test
    fun `getSongsByAlbumId - calls remote with correct albumId`() = runTest {
        coEvery { remoteDataSource.getSongsByCollectionId(albumId) } returns
                NetworkResponse.Success(listSongsResponse)
        coEvery { songsMapper.map(any()) } returns listOf(song)

        repository.getSongsByAlbumId(albumId).first()

        coVerify(exactly = 1) { remoteDataSource.getSongsByCollectionId(albumId) }
    }

    @Test
    fun `getSongsByAlbumId - propagates network error as exception`() = runTest {
        val error = Exception("Network failure")
        coEvery { remoteDataSource.getSongsByCollectionId(albumId) } returns
                NetworkResponse.Error(exception = error)

        val thrown = runCatching { repository.getSongsByAlbumId(albumId).first() }.exceptionOrNull()

        assertThat(thrown?.message).isEqualTo(error.message)
    }

    // -----------------------------------------------------------------------
    // getSongsByAlbumIdFromDb
    // -----------------------------------------------------------------------

    @Test
    fun `getSongsByAlbumIdFromDb - returns mapped songs from db`() = runTest {
        coEvery { songDao.getSongsByAlbumId(albumId) } returns listOf(songEntity)

        val result = repository.getSongsByAlbumIdFromDb(albumId)

        assertThat(result.first()).isEqualTo(song)
    }

    @Test
    fun `getSongsByAlbumIdFromDb - returns empty list when db has no songs`() = runTest {
        coEvery { songDao.getSongsByAlbumId(albumId) } returns emptyList()

        val result = repository.getSongsByAlbumIdFromDb(albumId)

        assertThat(result).isEqualTo(emptyList<Song>())
    }

    // -----------------------------------------------------------------------
    // getAlbumFromDb
    // -----------------------------------------------------------------------

    @Test
    fun `getAlbumFromDb - returns mapped AlbumInfo when album exists`() = runTest {
        coEvery { albumDao.getAlbumById(albumId) } returns albumEntity

        val result = repository.getAlbumFromDb(albumId)

        assertThat(result).isEqualTo(albumInfo)
    }

    @Test
    fun `getAlbumFromDb - returns null when album does not exist`() = runTest {
        coEvery { albumDao.getAlbumById(albumId) } returns null

        val result = repository.getAlbumFromDb(albumId)

        assertThat(result).isNull()
    }

    // -----------------------------------------------------------------------
    // saveSongsByAlbum
    // -----------------------------------------------------------------------

    @Test
    fun `saveSongsByAlbum - inserts songs with correct albumId as searchTerm`() = runTest {
        repository.saveSongsByAlbum(albumId, listOf(song))

        coVerify(exactly = 1) {
            songDao.insertSongs(match { entities ->
                entities.size == 1 &&
                entities[0].trackId == song.trackId &&
                entities[0].searchTerm == albumId
            })
        }
    }

    @Test
    fun `saveSongsByAlbum - inserts all songs when list has multiple items`() = runTest {
        repository.saveSongsByAlbum(albumId, SongMock.songList)

        coVerify(exactly = 1) {
            songDao.insertSongs(match { it.size == SongMock.songList.size })
        }
    }

    @Test
    fun `saveSongsByAlbum - does nothing when songs list is empty`() = runTest {
        repository.saveSongsByAlbum(albumId, emptyList())

        coVerify(exactly = 1) { songDao.insertSongs(emptyList()) }
    }

    // -----------------------------------------------------------------------
    // saveAlbum
    // -----------------------------------------------------------------------

    @Test
    fun `saveAlbum - inserts correct album entity`() = runTest {
        repository.saveAlbum(albumInfo)

        coVerify(exactly = 1) {
            albumDao.insertAlbum(match { entity ->
                entity.collectionId == albumInfo.collectionId &&
                entity.title == albumInfo.title &&
                entity.artistName == albumInfo.artistName &&
                entity.coverUrl == albumInfo.coverUrl
            })
        }
    }
}
