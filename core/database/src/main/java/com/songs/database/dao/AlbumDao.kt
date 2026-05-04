package com.songs.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.songs.database.entity.AlbumEntity

@Dao
interface AlbumDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbum(album: AlbumEntity)

    @Query("SELECT * FROM albums WHERE collectionId = :collectionId LIMIT 1")
    suspend fun getAlbumById(collectionId: String): AlbumEntity?
}
