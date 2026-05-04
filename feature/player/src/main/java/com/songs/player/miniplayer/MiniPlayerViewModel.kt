package com.songs.player.miniplayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.songs.player.media.MediaPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MiniPlayerViewModel @Inject constructor(
    private val mediaPlayerWrapper: MediaPlayer,
) : ViewModel() {

    private var _lastTrackIds: List<Long> = emptyList()
    private var _lastCurrentTrackId: Long = 0L

    val uiState: StateFlow<MiniPlayerUiState> = mediaPlayerWrapper.state
        .map { playerState ->
            MiniPlayerUiState(
                isVisible = playerState.trackTitle != null,
                trackTitle = playerState.trackTitle,
                artistName = playerState.artistName,
                artworkUrl = playerState.artworkUrl,
                isPlaying = playerState.isPlaying,
                trackIds = _lastTrackIds,
                currentTrackId = _lastCurrentTrackId,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MiniPlayerUiState(),
        )

    fun updatePlayerRoute(trackIds: List<Long>, currentTrackId: Long) {
        _lastTrackIds = trackIds
        _lastCurrentTrackId = currentTrackId
    }

    fun togglePlayPause() {
        mediaPlayerWrapper.togglePlayPause()
    }
}
