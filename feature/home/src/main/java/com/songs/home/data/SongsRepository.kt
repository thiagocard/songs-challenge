package com.songs.home.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.songs.database.SongsDatabase
import com.songs.database.dao.AlbumDao
import com.songs.database.dao.SongDao
import com.songs.home.data.local.toDomain
import com.songs.home.data.local.toEntity
import com.songs.home.data.remote.SongsRemoteDataSource
import com.songs.home.domain.mapper.SongsMapper
import com.songs.home.domain.model.AlbumInfo
import com.songs.home.domain.model.Song
import com.songs.networking.toFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface SongsRepository {

    fun getSongsPagingFlow(term: String): Flow<PagingData<Song>>

    suspend fun getSongsByAlbumId(albumId: String): Flow<List<Song>>

    suspend fun getSongsByAlbumIdFromDb(albumId: String): List<Song>

    suspend fun getAlbumFromDb(albumId: String): AlbumInfo?

    suspend fun saveSongsByAlbum(albumId: String, songs: List<Song>)

    suspend fun saveAlbum(album: AlbumInfo)
}

class SongsRepositoryImpl @Inject constructor(
    private val songsRemoteDataSource: SongsRemoteDataSource,
    private val songsMapper: SongsMapper,
    private val songDao: SongDao,
    private val albumDao: AlbumDao,
    private val database: SongsDatabase,
) : SongsRepository {

    @OptIn(ExperimentalPagingApi::class)
    override fun getSongsPagingFlow(term: String): Flow<PagingData<Song>> =
        Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                initialLoadSize = PAGE_SIZE * 2,
                prefetchDistance = 2,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = { songDao.pagingSourceByTerm(term) },
            remoteMediator = SongsRemoteMediator(
                term = term,
                remoteDataSource = songsRemoteDataSource,
                songsMapper = songsMapper,
                database = database,
            ),
        ).flow.map { pagingData -> pagingData.map { it.toDomain() } }

    override suspend fun getSongsByAlbumId(albumId: String): Flow<List<Song>> =
        songsRemoteDataSource.getSongsByCollectionId(albumId)
            .toFlow()
            .map { songsMapper.map(it) }

    override suspend fun getSongsByAlbumIdFromDb(albumId: String): List<Song> =
        songDao.getSongsByAlbumId(albumId).map { it.toDomain() }

    override suspend fun getAlbumFromDb(albumId: String): AlbumInfo? =
        albumDao.getAlbumById(albumId)?.toDomain()

    override suspend fun saveSongsByAlbum(albumId: String, songs: List<Song>) {
        songDao.insertSongs(songs.map { it.toEntity(albumId) })
    }

    override suspend fun saveAlbum(album: AlbumInfo) {
        albumDao.insertAlbum(album.toEntity())
    }

    private companion object {
        const val PAGE_SIZE = 10
    }
}
