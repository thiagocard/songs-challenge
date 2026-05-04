package com.songs.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remote_keys")
data class RemoteKeysEntity(
    @PrimaryKey val searchTerm: String,
    val nextOffset: Int?,
    val prevOffset: Int?
)
