package com.songs.home.di

import com.songs.home.data.SongsRepository
import com.songs.home.data.SongsRepositoryImpl
import com.songs.home.data.remote.SongsRemoteDataSource
import com.songs.home.data.remote.SongsRemoteDataSourceImpl
import com.songs.home.domain.mapper.SongsMapper
import com.songs.home.domain.mapper.SongsMapperImpl
import com.songs.home.domain.usecase.GetSongsByAlbumUseCase
import com.songs.home.domain.usecase.GetSongsByAlbumUseCaseImpl
import com.songs.home.domain.usecase.GetSongsUseCase
import com.songs.home.domain.usecase.GetSongsUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class SongsModule {

    @Binds
    @Singleton
    abstract fun bindSongsRepository(impl: SongsRepositoryImpl): SongsRepository

    @Binds
    @Singleton
    abstract fun bindGetSongsUseCase(impl: GetSongsUseCaseImpl): GetSongsUseCase

    @Binds
    @Singleton
    abstract fun bindGetSongsByAlbumUseCase(impl: GetSongsByAlbumUseCaseImpl): GetSongsByAlbumUseCase

    @Binds
    @Singleton
    abstract fun bindSongsMapper(impl: SongsMapperImpl): SongsMapper

    @Binds
    @Singleton
    abstract fun bindSongsRemoteDataSource(impl: SongsRemoteDataSourceImpl): SongsRemoteDataSource
}
