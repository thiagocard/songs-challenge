package com.songs.networking.di

import com.songs.core.networking.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.resources.Resources
import io.ktor.client.request.accept
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @QualifierHost
    fun provideHost(): String = BuildConfig.HOST

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideHttpClient(
        json: Json,
        @QualifierHost baseUrl: String,
    ): HttpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            // iTunes API returns "text/javascript" content type, so we need to set it manually
            json(json, contentType = ContentType.Text.JavaScript)
        }
        install(Logging) {
            logger = Logger.ANDROID
            level = if (BuildConfig.DEBUG) LogLevel.INFO else LogLevel.NONE
        }
        install(Resources)
        defaultRequest {
            url(baseUrl)
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
        }
    }
}
