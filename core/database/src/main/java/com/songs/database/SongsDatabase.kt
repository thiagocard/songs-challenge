package com.songs.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.songs.database.dao.AlbumDao
import com.songs.database.dao.RemoteKeysDao
import com.songs.database.dao.SearchResultDao
import com.songs.database.dao.SongDao
import com.songs.database.dao.TrackDao
import com.songs.database.entity.AlbumEntity
import com.songs.database.entity.RemoteKeysEntity
import com.songs.database.entity.SearchResultEntity
import com.songs.database.entity.SongEntity
import com.songs.database.entity.TrackEntity

@Database(
    entities = [SongEntity::class, TrackEntity::class, RemoteKeysEntity::class, SearchResultEntity::class, AlbumEntity::class],
    version = 7,
    exportSchema = false
)
abstract class SongsDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun trackDao(): TrackDao
    abstract fun remoteKeysDao(): RemoteKeysDao
    abstract fun searchResultDao(): SearchResultDao
    abstract fun albumDao(): AlbumDao
}
