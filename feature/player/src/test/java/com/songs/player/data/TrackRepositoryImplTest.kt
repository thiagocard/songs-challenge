package com.songs.player.data

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.songs.database.dao.SongDao
import com.songs.database.dao.TrackDao
import com.songs.database.entity.TrackEntity
import com.songs.networking.NetworkResponse
import com.songs.player.data.model.ListTrackResponse
import com.songs.player.data.remote.PlayerRemoteDataSource
import com.songs.support.mock.TrackMock
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class TrackRepositoryImplTest {

    private lateinit var remoteDataSource: PlayerRemoteDataSource
    private lateinit var trackDao: TrackDao
    private lateinit var songDao: SongDao
    private lateinit var repository: TrackRepositoryImpl

    private val track = TrackMock.track
    private val trackId = track.trackId!!

    @Before
    fun setUp() {
        remoteDataSource = mockk(relaxed = true)
        trackDao = mockk(relaxed = true)
        songDao = mockk(relaxed = true)
        repository = TrackRepositoryImpl(remoteDataSource, trackDao, songDao)
    }

    @Test
    fun getTrackFetchesFromRemote() = runTest {
        val response = ListTrackResponse(resultCount = 1, results = listOf())
        coEvery { remoteDataSource.getTrack(trackId.toString()) } returns NetworkResponse.Success(response)

        repository.getTrack(trackId.toString()).test {
            awaitItem()
            awaitComplete()
        }

        coVerify(exactly = 1) { remoteDataSource.getTrack(trackId.toString()) }
    }

    @Test
    fun getTrackFromDbDelegatesToTrackDao() = runTest {
        val entity = TrackEntity(
            trackId = trackId, wrapperType = track.wrapperType, kind = track.kind,
            artistId = track.artistId, collectionId = track.collectionId,
            artistName = track.artistName, collectionName = track.collectionName,
            trackName = track.trackName, collectionCensoredName = track.collectionCensoredName,
            trackCensoredName = track.trackCensoredName, artistViewUrl = track.artistViewUrl,
            collectionArtistId = track.collectionArtistId,
            collectionArtistViewUrl = track.collectionArtistViewUrl,
            collectionViewUrl = track.collectionViewUrl, trackViewUrl = track.trackViewUrl,
            previewUrl = track.previewUrl, artworkUrl30 = track.artworkUrl30,
            artworkUrl60 = track.artworkUrl60, artworkUrl100 = track.artworkUrl100,
            artworkUrl500 = track.artworkUrl500, collectionPrice = track.collectionPrice,
            trackPrice = track.trackPrice, trackRentalPrice = track.trackRentalPrice,
            collectionHdPrice = track.collectionHdPrice, trackHdPrice = track.trackHdPrice,
            trackHdRentalPrice = track.trackHdRentalPrice, releaseDate = track.releaseDate,
            collectionExplicitness = track.collectionExplicitness,
            trackExplicitness = track.trackExplicitness, discCount = track.discCount,
            discNumber = track.discNumber, trackCount = track.trackCount,
            trackNumber = track.trackNumber, trackTimeMillis = track.trackTimeMillis,
            country = track.country, currency = track.currency,
            primaryGenreName = track.primaryGenreName,
            contentAdvisoryRating = track.contentAdvisoryRating,
            shortDescription = track.shortDescription, longDescription = track.longDescription,
            hasITunesExtras = track.hasITunesExtras, isStreamable = track.isStreamable,
        )
        coEvery { trackDao.getTrackById(trackId) } returns listOf(entity)

        val result = repository.getTrackFromDb(trackId)

        assertThat(result.size).isEqualTo(1)
        assertThat(result[0].trackId).isEqualTo(trackId)
        coVerify(exactly = 1) { trackDao.getTrackById(trackId) }
    }

    @Test
    fun getTrackFromDbReturnsEmptyWhenDaoReturnsEmpty() = runTest {
        coEvery { trackDao.getTrackById(trackId) } returns emptyList()
        val result = repository.getTrackFromDb(trackId)
        assertThat(result.size).isEqualTo(0)
    }

    @Test
    fun getTrackFromSongsDbDelegatesToSongDao() = runTest {
        coEvery { songDao.getSongById(trackId) } returns emptyList()
        val result = repository.getTrackFromSongsDb(trackId)
        assertThat(result.size).isEqualTo(0)
        coVerify(exactly = 1) { songDao.getSongById(trackId) }
    }

    @Test
    fun saveTracksDelegatesToTrackDao() = runTest {
        repository.saveTracks(listOf(track))
        coVerify(exactly = 1) { trackDao.insertTracks(any()) }
    }
}
