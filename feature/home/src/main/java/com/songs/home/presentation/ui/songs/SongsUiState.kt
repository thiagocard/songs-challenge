package com.songs.home.presentation.ui.songs

internal data class SongsUiState(
    val searchTerm: String = "",
    val nowPlayingTrackId: Long? = null,
)
