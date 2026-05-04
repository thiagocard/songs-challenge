package com.songs.home.presentation.ui.album

import com.songs.home.domain.model.AlbumWithSongs

sealed interface AlbumUiState {
    data object Loading : AlbumUiState
    data class Success(
        val albumWithSongs: AlbumWithSongs,
    ) : AlbumUiState
    data object Error : AlbumUiState
}
