package com.songs.player.domain.usecase

import com.songs.player.data.TrackRepository
import com.songs.player.domain.model.Track
import kotlinx.coroutines.flow.Flow import kotlinx.coroutines.flow.flow

interface GetTrackByIdUseCase {
    suspend operator fun invoke(trackId: String): Flow<List<Track>>
}

class GetTrackByIdUseCaseImpl @javax.inject.Inject constructor(
    private val songsRepository: TrackRepository,
) : GetTrackByIdUseCase {
    override suspend fun invoke(trackId: String): Flow<List<Track>> {
        val id = trackId.toLongOrNull() ?: 0L

        return flow {
            // 1. Emit from dedicated tracks table immediately if available
            val cachedTrack = songsRepository.getTrackFromDb(id)
            if (cachedTrack.isNotEmpty()) {
                emit(cachedTrack)
                return@flow
            }

            // 2. Emit from songs table (populated by home pager) immediately if available
            val cachedSong = songsRepository.getTrackFromSongsDb(id)
            if (cachedSong.isNotEmpty()) {
                emit(cachedSong)
            }

            // 3. Try network – update UI with fresh data and persist to tracks table
            try {
                songsRepository.getTrack(trackId).collect { tracks ->
                    if (tracks.isNotEmpty()) {
                        songsRepository.saveTracks(tracks)
                        emit(tracks)
                    }
                }
            } catch (e: Exception) {
                // If we already emitted cached data, swallow the network error silently.
                // If nothing was emitted yet, propagate so the ViewModel can show an error.
                if (cachedSong.isEmpty()) throw e
            }
        }
    }
}
