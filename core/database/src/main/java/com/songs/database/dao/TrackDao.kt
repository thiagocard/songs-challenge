package com.songs.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.songs.database.entity.TrackEntity

@Dao
interface TrackDao {

    @Query("SELECT * FROM tracks WHERE trackId = :trackId")
    suspend fun getTrackById(trackId: Long): List<TrackEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<TrackEntity>)
}
