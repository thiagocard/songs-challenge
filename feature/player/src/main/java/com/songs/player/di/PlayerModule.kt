package com.songs.player.di

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.songs.common.coroutine.DispatcherProvider
import com.songs.common.playback.NowPlayingProvider
import com.songs.player.data.TrackRepository
import com.songs.player.data.TrackRepositoryImpl
import com.songs.player.data.remote.PlayerRemoteDataSource
import com.songs.player.data.remote.PlayerRemoteDataSourceImpl
import com.songs.player.domain.usecase.GetTrackByIdUseCase
import com.songs.player.domain.usecase.GetTrackByIdUseCaseImpl
import com.songs.player.media.MediaPlayer
import com.songs.player.media.MediaPlayerImpl
import com.songs.player.playback.MediaPlayerNowPlayingProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

private const val AUDIO_CACHE_SIZE_BYTES = 50L * 1024 * 1024 // 50 MB

@Module
@InstallIn(SingletonComponent::class)
internal abstract class PlayerModule {

    @Binds
    @Singleton
    abstract fun bindPlayerRemoteDataSource(impl: PlayerRemoteDataSourceImpl): PlayerRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindTrackRepository(impl: TrackRepositoryImpl): TrackRepository

    @Binds
    @Singleton
    abstract fun bindGetTrackByIdUseCase(impl: GetTrackByIdUseCaseImpl): GetTrackByIdUseCase

    @Binds
    @Singleton
    abstract fun bindNowPlayingProvider(
        impl: MediaPlayerNowPlayingProvider
    ): NowPlayingProvider

    companion object {
        @OptIn(UnstableApi::class)
        @Provides
        @Singleton
        fun provideAudioCache(@ApplicationContext context: Context): SimpleCache =
            SimpleCache(
                File(context.cacheDir, "audio"),
                LeastRecentlyUsedCacheEvictor(AUDIO_CACHE_SIZE_BYTES),
                StandaloneDatabaseProvider(context),
            )

        @OptIn(UnstableApi::class)
        @Provides
        @Singleton
        fun provideExoPlayer(
            @ApplicationContext context: Context,
            simpleCache: SimpleCache,
        ): ExoPlayer {
            val upstreamFactory = DefaultDataSource.Factory(context)
            val cacheDataSourceFactory = CacheDataSource.Factory()
                .setCache(simpleCache)
                .setUpstreamDataSourceFactory(upstreamFactory)
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
            return ExoPlayer.Builder(context)
                .setMediaSourceFactory(
                    DefaultMediaSourceFactory(context).setDataSourceFactory(cacheDataSourceFactory)
                )
                .build()
        }

        @Provides
        @Singleton
        fun provideMediaPlayerWrapper(
            @ApplicationContext context: Context,
            dispatcherProvider: DispatcherProvider,
        ): MediaPlayer = MediaPlayerImpl(context, dispatcherProvider)
    }
}
