package com.songs.common.di

import com.songs.common.coroutine.DispatcherProvider
import com.songs.common.coroutine.DispatcherProviderImpl
import com.songs.common.resource.ResourceProvider
import com.songs.common.resource.ResourceProviderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CommonModule {

    @Binds
    @Singleton
    abstract fun bindResourceProvider(impl: ResourceProviderImpl): ResourceProvider

    @Binds
    @Singleton
    abstract fun bindDispatcherProvider(impl: DispatcherProviderImpl): DispatcherProvider
}
