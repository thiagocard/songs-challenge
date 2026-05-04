package com.songs.player.data.remote

import com.songs.networking.NetworkResponse
import com.songs.networking.adapter.safeApiCall
import com.songs.player.data.model.ListTrackResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import javax.inject.Inject

internal class PlayerRemoteDataSourceImpl @Inject constructor(
    private val httpClient: HttpClient,
) : PlayerRemoteDataSource {

    override suspend fun getTrack(trackId: String): NetworkResponse<ListTrackResponse> =
        safeApiCall {
            httpClient.get(ENDPOINT_LOOKUP) {
                parameter(PARAM_ENTITY, ENTITY_SONG)
                parameter(PARAM_ID, trackId)
            }
        }
    
    private companion object {
        const val ENDPOINT_LOOKUP = "lookup"
        const val PARAM_ENTITY = "entity"
        const val PARAM_ID = "id"
        const val ENTITY_SONG = "song"
    }
}
