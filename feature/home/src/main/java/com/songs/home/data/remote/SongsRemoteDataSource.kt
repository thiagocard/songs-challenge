package com.songs.home.data.remote
import com.songs.home.data.model.ListSongsResponse
import com.songs.networking.NetworkResponse
import com.songs.networking.adapter.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import javax.inject.Inject

interface SongsRemoteDataSource {
    suspend fun getSongs(
        term: String,
        limit: Int = 10,
        offset: Int = 0
    ): NetworkResponse<ListSongsResponse>
    suspend fun getSongsByCollectionId(
        collectionId: String
    ): NetworkResponse<ListSongsResponse>
}

internal class SongsRemoteDataSourceImpl @Inject constructor(
    private val httpClient: HttpClient,
) : SongsRemoteDataSource {

    override suspend fun getSongs(
        term: String,
        limit: Int,
        offset: Int,
    ): NetworkResponse<ListSongsResponse> = safeApiCall {
        httpClient.get(ENDPOINT_SEARCH) {
            parameter(PARAM_ENTITY, ENTITY_SONG)
            parameter(PARAM_TERM, term)
            parameter(PARAM_LIMIT, limit)
            parameter(PARAM_OFFSET, offset)
        }
    }

    override suspend fun getSongsByCollectionId(
        collectionId: String,
    ): NetworkResponse<ListSongsResponse> = safeApiCall {
        httpClient.get(ENDPOINT_LOOKUP) {
            parameter(PARAM_ENTITY, ENTITY_SONG)
            parameter(PARAM_ID, collectionId)
        }
    }

    private companion object {
        const val ENDPOINT_SEARCH = "search"
        const val ENDPOINT_LOOKUP = "lookup"
        const val PARAM_ENTITY = "entity"
        const val PARAM_TERM = "term"
        const val PARAM_LIMIT = "limit"
        const val PARAM_OFFSET = "offset"
        const val PARAM_ID = "id"
        const val ENTITY_SONG = "song"
    }
}
