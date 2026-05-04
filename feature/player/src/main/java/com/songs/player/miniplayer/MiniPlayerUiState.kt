package com.songs.player.miniplayer

data class MiniPlayerUiState(
    val isVisible: Boolean = false,
    val trackTitle: String? = null,
    val artistName: String? = null,
    val artworkUrl: String? = null,
    val isPlaying: Boolean = false,
    val trackIds: List<Long> = emptyList(),
    val currentTrackId: Long = 0L,
)
