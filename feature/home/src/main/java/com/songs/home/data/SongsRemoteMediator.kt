package com.songs.home.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.songs.database.SongsDatabase
import com.songs.database.entity.RemoteKeysEntity
import com.songs.database.entity.SearchResultEntity
import com.songs.database.entity.SongEntity
import com.songs.home.data.local.toEntity
import com.songs.home.data.remote.SongsRemoteDataSource
import com.songs.home.domain.mapper.SongsMapper
import com.songs.networking.NetworkResponse

@OptIn(ExperimentalPagingApi::class)
internal class SongsRemoteMediator(
    private val term: String,
    private val remoteDataSource: SongsRemoteDataSource,
    private val songsMapper: SongsMapper,
    private val database: SongsDatabase,
) : RemoteMediator<Int, SongEntity>() {

    private val remoteKeysDao = database.remoteKeysDao()
    private val songDao = database.songDao()
    private val searchResultDao = database.searchResultDao()

    /**
     * Called once per new paging source (including after invalidations).
     * - No cache → LAUNCH_INITIAL_REFRESH: fetch from network.
     * - Cache exists → SKIP_INITIAL_REFRESH: serve from DB immediately.
     *   This prevents an infinite loop where a DB write invalidates the paging source,
     *   which triggers another REFRESH, ad infinitum.
     *   Pull-to-refresh calls load(REFRESH) directly and bypasses this method.
     */
    override suspend fun initialize(): InitializeAction {
        val hasCachedData = remoteKeysDao.getRemoteKeys(term) != null
        return if (hasCachedData) InitializeAction.SKIP_INITIAL_REFRESH
        else InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, SongEntity>,
    ): MediatorResult {
        val offset: Int = when (loadType) {
            LoadType.REFRESH -> 0
            // Pagination is append-only.
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
                val keys = remoteKeysDao.getRemoteKeys(term)
                // null nextOffset means end of pagination was already reached.
                keys?.nextOffset ?: return MediatorResult.Success(endOfPaginationReached = true)
            }
        }

        val limit = if (loadType == LoadType.REFRESH) state.config.initialLoadSize
                    else state.config.pageSize

        return when (val response = remoteDataSource.getSongs(term, limit, offset)) {
            is NetworkResponse.Error -> MediatorResult.Error(Exception("Failed to fetch more songs for term $term"))
            is NetworkResponse.Success -> {
                val songs = songsMapper.map(response.value)
                val endOfPagination = songs.size < limit

                database.withTransaction {
                    if (loadType == LoadType.REFRESH) {
                        // Wipe existing cached data for this term before storing fresh results.
                        remoteKeysDao.deleteByTerm(term)
                        searchResultDao.deleteByTerm(term)
                        songDao.deleteSongsByTerm(term)
                    }

                    // --- songs table: deduplicated song data (REPLACE updates stale metadata) ---
                    songDao.insertSongs(songs.map { it.toEntity(term) })

                    // --- search_results table: ordered positions mirroring the API response ---
                    // nextPositionStart is the count of positions already stored for this term,
                    // so new entries are appended after existing ones without gaps.
                    val nextPositionStart = searchResultDao.countByTerm(term)
                    searchResultDao.insertAll(
                        songs.mapIndexed { index, song ->
                            SearchResultEntity(
                                searchTerm = term,
                                position = nextPositionStart + index,
                                trackId = song.trackId ?: 0L,
                            )
                        }
                    )

                    remoteKeysDao.insertOrReplace(
                        RemoteKeysEntity(
                            searchTerm = term,
                            nextOffset = if (endOfPagination) null else offset + songs.size,
                            prevOffset = null,
                        )
                    )
                }

                MediatorResult.Success(endOfPaginationReached = endOfPagination)
            }
        }
    }
}
