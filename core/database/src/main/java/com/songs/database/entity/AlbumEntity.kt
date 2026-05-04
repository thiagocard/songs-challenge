package com.songs.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "albums")
data class AlbumEntity(
    @PrimaryKey val collectionId: Long,
    val title: String,
    val artistName: String,
    val coverUrl: String?,
)
