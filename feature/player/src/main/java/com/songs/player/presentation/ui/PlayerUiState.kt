package com.songs.player.presentation.ui

import com.songs.player.domain.model.Track

internal sealed interface PlayerUiState {
    data object Loading : PlayerUiState
    data class Success(
        val currentTrack: Track? = null,
        val isPlaying: Boolean = false,
        val currentPosition: Long = 0L,
        val formattedCurrentPosition: String = "",
        val remainingTime: Long = 0L,
        val formattedRemainingTime: String = "",
        val duration: Long = 0L,
        val sliderPosition: Float = 0f,
        val isRepeatOn: Boolean = false,
    ) : PlayerUiState
    data object Error : PlayerUiState
}
