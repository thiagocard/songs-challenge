package com.songs.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.songs.database.entity.RemoteKeysEntity

@Dao
interface RemoteKeysDao {

    @Query("SELECT * FROM remote_keys WHERE searchTerm = :term")
    suspend fun getRemoteKeys(term: String): RemoteKeysEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(remoteKeys: RemoteKeysEntity)

    @Query("DELETE FROM remote_keys WHERE searchTerm = :term")
    suspend fun deleteByTerm(term: String)
}
