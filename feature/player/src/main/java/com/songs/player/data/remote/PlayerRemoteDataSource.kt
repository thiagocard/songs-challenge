package com.songs.player.data.remote

import com.songs.networking.NetworkResponse
import com.songs.player.data.model.ListTrackResponse

interface PlayerRemoteDataSource {
    suspend fun getTrack(trackId: String): NetworkResponse<ListTrackResponse>
}
