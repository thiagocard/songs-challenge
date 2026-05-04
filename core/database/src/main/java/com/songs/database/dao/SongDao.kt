package com.songs.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.songs.database.entity.SongEntity

@Dao
interface SongDao {

    /**
     * Paging source that returns songs ordered by their position in the iTunes API response.
     * Joins `search_results` (which preserves order and allows duplicates) with `songs`
     * (which holds deduplicated song data).
     */
    @Query("""
        SELECT songs.* FROM search_results
        INNER JOIN songs
            ON search_results.trackId = songs.trackId
           AND search_results.searchTerm = songs.searchTerm
        WHERE search_results.searchTerm = :term
        ORDER BY search_results.position ASC
    """)
    fun pagingSourceByTerm(term: String): PagingSource<Int, SongEntity>

    /** Returns a page of deduplicated songs for [term] ordered by insertion time. */
    @Query("SELECT * FROM songs WHERE searchTerm = :term ORDER BY rowid ASC LIMIT :limit OFFSET :offset")
    suspend fun getSongsByTermPaged(term: String, limit: Int, offset: Int): List<SongEntity>

    /** Total number of unique songs stored for [term]. Used to derive the next API offset. */
    @Query("SELECT COUNT(*) FROM songs WHERE searchTerm = :term")
    suspend fun countSongsByTerm(term: String): Int

    @Query("SELECT * FROM songs WHERE albumId = :albumId AND searchTerm = :albumId")
    suspend fun getSongsByAlbumId(albumId: String): List<SongEntity>

    @Query("SELECT * FROM songs WHERE trackId = :trackId LIMIT 1")
    suspend fun getSongById(trackId: Long): List<SongEntity>

    /** Insert or replace songs (used for search-term caching and album caching). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<SongEntity>)

    @Query("DELETE FROM songs WHERE searchTerm = :term")
    suspend fun deleteSongsByTerm(term: String)
}
