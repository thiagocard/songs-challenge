
package com.songs.player.data

import com.songs.database.dao.SongDao
import com.songs.database.dao.TrackDao
import com.songs.networking.toFlow
import com.songs.player.data.local.toDomain
import com.songs.player.data.local.toEntity
import com.songs.player.data.remote.PlayerRemoteDataSource
import com.songs.player.domain.model.Track
import com.songs.player.domain.model.toListTracks
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface TrackRepository {
    suspend fun getTrack(trackId: String): Flow<List<Track>>
    suspend fun getTrackFromDb(trackId: Long): List<Track>
    suspend fun getTrackFromSongsDb(trackId: Long): List<Track>
    suspend fun saveTracks(tracks: List<Track>)
}

class TrackRepositoryImpl @Inject constructor(
    private val remoteDataSource: PlayerRemoteDataSource,
    private val trackDao: TrackDao,
    private val songDao: SongDao,
) : TrackRepository {

    override suspend fun getTrack(trackId: String): Flow<List<Track>> {
        return remoteDataSource.getTrack(trackId).toFlow().map { it.toListTracks() }
    }

    override suspend fun getTrackFromDb(trackId: Long): List<Track> {
        return trackDao.getTrackById(trackId).map { it.toDomain() }
    }

    override suspend fun getTrackFromSongsDb(trackId: Long): List<Track> {
        return songDao.getSongById(trackId).map { it.toDomain() }
    }

    override suspend fun saveTracks(tracks: List<Track>) {
        trackDao.insertTracks(tracks.map { it.toEntity() })
    }
}
