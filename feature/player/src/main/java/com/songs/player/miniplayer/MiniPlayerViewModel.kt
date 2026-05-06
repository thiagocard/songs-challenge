package com.songs.player.miniplayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.songs.common.playback.NowPlayingProvider
import com.songs.player.media.MediaPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MiniPlayerViewModel @Inject constructor(
    private val mediaPlayerWrapper: MediaPlayer,
    nowPlayingProvider: NowPlayingProvider,
) : ViewModel() {

    val uiState: StateFlow<MiniPlayerUiState> = combine(
        mediaPlayerWrapper.state,
        nowPlayingProvider.nowPlaying,
    ) { playerState, nowPlaying ->
        MiniPlayerUiState(
            isVisible = nowPlaying != null,
            trackTitle = nowPlaying?.title,
            artistName = nowPlaying?.artistName,
            artworkUrl = nowPlaying?.artworkUrl,
            isPlaying = playerState.isPlaying,
            trackIds = nowPlaying?.trackIds ?: emptyList(),
            currentTrackId = nowPlaying?.trackId ?: 0L,
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MiniPlayerUiState(),
        )

    fun togglePlayPause() {
        mediaPlayerWrapper.togglePlayPause()
    }
}
