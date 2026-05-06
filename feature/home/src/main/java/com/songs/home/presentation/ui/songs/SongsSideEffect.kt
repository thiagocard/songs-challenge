package com.songs.home.presentation.ui.songs

internal sealed interface SongsSideEffect {
    data class NavigateToPlayer(
        val trackIds: List<Long>,
        val currentTrackId: Long,
    ) : SongsSideEffect
}
