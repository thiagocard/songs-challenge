package com.songs.player.presentation.ui

import com.songs.player.domain.model.Track

sealed interface PlaylistUiState {
    data object Loading : PlaylistUiState
    data class Success(
        val playableTracks: List<Track> = emptyList()
    ) : PlaylistUiState
    data object Error : PlaylistUiState
}
