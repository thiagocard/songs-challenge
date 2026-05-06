package com.songs.player.presentation.ui

sealed interface PlayerSideEffect {
    data class NavigateToPlayer(
        val trackIds: List<Long>,
        val currentTrackId: Long
    ) : PlayerSideEffect
}
