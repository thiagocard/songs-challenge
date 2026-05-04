package com.songs.home.domain.usecase

import com.songs.home.data.SongsRepository
import com.songs.home.domain.model.AlbumInfo
import com.songs.home.domain.model.AlbumWithSongs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

interface GetSongsByAlbumUseCase {
    suspend operator fun invoke(albumId: String): Flow<AlbumWithSongs>
}

class GetSongsByAlbumUseCaseImpl @javax.inject.Inject constructor(
    private val songsRepository: SongsRepository,
) : GetSongsByAlbumUseCase {

    override suspend fun invoke(albumId: String): Flow<AlbumWithSongs> {
        val cached = songsRepository.getSongsByAlbumIdFromDb(albumId)
            .filter { it.wrapperType == WRAPPER_TYPE_TRACK }
        if (cached.isNotEmpty()) {
            val album = songsRepository.getAlbumFromDb(albumId) ?: return emptyFlow()
            return flowOf(AlbumWithSongs(songs = cached, album = album))
        }
        return songsRepository.getSongsByAlbumId(albumId)
            .map { songs ->
                AlbumWithSongs(
                    songs = songs.filter { it.wrapperType == WRAPPER_TYPE_TRACK },
                    album = songs
                        .firstOrNull { it.wrapperType == WRAPPER_TYPE_COLLECTION }
                        ?.let { song ->
                            AlbumInfo(
                                collectionId = song.collectionId ?: -1L,
                                title = song.collectionName.orEmpty(),
                                coverUrl = song.artworkUrl100,
                                artistName = song.artistName,
                            )
                        } ?: error("Album data not found in the response for albumId: $albumId"),
                )
            }
            .onEach { albumWithSongs ->
                if (albumWithSongs.songs.isNotEmpty()) {
                    songsRepository.saveSongsByAlbum(albumId, albumWithSongs.songs)
                    albumWithSongs.album.let { songsRepository.saveAlbum(it) }
                }
            }
    }

    private companion object {
        const val WRAPPER_TYPE_TRACK = "track"
        const val WRAPPER_TYPE_COLLECTION = "collection"
    }
}
