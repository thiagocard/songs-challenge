package com.songs.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.songs.database.entity.SearchResultEntity

@Dao
interface SearchResultDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<SearchResultEntity>)

    @Query("DELETE FROM search_results WHERE searchTerm = :term")
    suspend fun deleteByTerm(term: String)

    /** Returns the number of positions already recorded for [term], i.e. the next free position index. */
    @Query("SELECT COUNT(*) FROM search_results WHERE searchTerm = :term")
    suspend fun countByTerm(term: String): Int
}
