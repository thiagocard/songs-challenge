package com.songs.database.di

import android.content.Context
import androidx.room.Room
import com.songs.database.SongsDatabase
import com.songs.database.dao.AlbumDao
import com.songs.database.dao.RemoteKeysDao
import com.songs.database.dao.SongDao
import com.songs.database.dao.TrackDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): SongsDatabase =
        Room.databaseBuilder(context, SongsDatabase::class.java, "songs_db")
            .fallbackToDestructiveMigration(true)
            .build()

    @Provides
    fun provideSongDao(db: SongsDatabase): SongDao = db.songDao()

    @Provides
    fun provideTrackDao(db: SongsDatabase): TrackDao = db.trackDao()

    @Provides
    fun provideRemoteKeysDao(db: SongsDatabase): RemoteKeysDao = db.remoteKeysDao()

    @Provides
    fun provideAlbumDao(db: SongsDatabase): AlbumDao = db.albumDao()
}
