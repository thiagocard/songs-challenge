package com.songs.player.presentation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.songs.common.extension.formatTime
import com.songs.player.domain.model.Track
import com.songs.player.domain.usecase.GetTrackByIdUseCase
import com.songs.player.media.MediaPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val getTrackByIdUseCase: GetTrackByIdUseCase,
    private val mediaPlayer: MediaPlayer,
) : ViewModel() {

    private val _uiState = MutableStateFlow<PlayerUiState>(PlayerUiState.Loading)
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    val playbackError: SharedFlow<Unit> = mediaPlayer.playbackErrors

    private val _playlistUiState = MutableStateFlow<PlaylistUiState>(PlaylistUiState.Loading)
    val playlistUiState: StateFlow<PlaylistUiState> = _playlistUiState.asStateFlow()

    /**
     * Set by [requestPlay] when the user explicitly taps a song to start playback.
     * [loadTrack] will only call [playTrack] when this matches the requested track,
     * ensuring that back-navigation to the PlayerScreen never restarts audio.
     */
    private var pendingPlayTrackId: Long? = null

    init {
        observeMediaPlayer()
    }

    /**
     * Called by the UI when the user **explicitly** navigates to a track with intent to play.
     * Must be called before [loadTrack] for the same [trackId].
     */
    fun requestPlay(trackId: Long) {
        pendingPlayTrackId = trackId
    }

    fun loadTrack(currentTrackId: Long, trackIds: List<Long>) {
        val alreadyLoaded = mediaPlayer.state.value.currentTrackId == currentTrackId
        if (alreadyLoaded) {
            refreshPlaylist(currentTrackId, trackIds)
            return
        }

        // Consume the pending-play intent. If none was set for this track,
        // the screen is resuming via back-navigation — load metadata only, no playback.
        val shouldPlay = pendingPlayTrackId == currentTrackId
        pendingPlayTrackId = null

        _playlistUiState.value = PlaylistUiState.Loading
        // Keep the current Success state (with stale or null track) while loading so the
        // player screen doesn't flash a full-screen spinner during the transition animation.
        updateUiState { copy(currentTrack = null) }

        if (currentTrackId == 0L) {
            _playlistUiState.value = PlaylistUiState.Error
            return
        }

        viewModelScope.launch {
            try {
                val currentTrackFlow = getTrackByIdUseCase(currentTrackId.toString())
                val otherTracksFlows = trackIds
                    .filter { it != currentTrackId }
                    .map { trackId -> getTrackByIdUseCase(trackId.toString()) }

                if (otherTracksFlows.isEmpty()) {
                    currentTrackFlow
                        .catch {
                            _playlistUiState.value = PlaylistUiState.Error
                        }
                        .collect { tracks ->
                            if (tracks.isNotEmpty()) {
                                val currentTrack = tracks.find { it.trackId == currentTrackId }
                                    ?: tracks.first()
                                updateUiState { copy(currentTrack = currentTrack) }
                                if (shouldPlay) playTrack(currentTrack)
                                _playlistUiState.value = PlaylistUiState.Success(emptyList())
                            } else {
                                _playlistUiState.value = PlaylistUiState.Error
                            }
                        }
                } else {
                    combine(
                        currentTrackFlow,
                        *otherTracksFlows.toTypedArray()
                    ) { results ->
                        results.toList().flatten()
                    }
                        .catch { _playlistUiState.value = PlaylistUiState.Error }
                        .collect { allTracks ->
                            if (allTracks.isNotEmpty()) {
                                val orderByTrackId = trackIds.withIndex().associate { (index, id) -> id to index }
                                val orderedTracks = allTracks.sortedBy { track ->
                                    orderByTrackId[track.trackId ?: Long.MIN_VALUE] ?: Int.MAX_VALUE
                                }

                                val currentTrack = orderedTracks.find { it.trackId == currentTrackId }
                                    ?: orderedTracks.first()
                                updateUiState { copy(currentTrack = currentTrack) }
                                if (shouldPlay) playTrack(currentTrack)
                                _playlistUiState.value = PlaylistUiState.Success(orderedTracks)
                            } else {
                                _playlistUiState.value = PlaylistUiState.Error
                            }
                        }
                }
            } catch (_: Exception) {
                _playlistUiState.value = PlaylistUiState.Error
            }
        }
    }

    private fun observeMediaPlayer() {
        viewModelScope.launch {
            try {
                mediaPlayer.state.collect { playerState ->
                    updateUiState {
                        val newDuration = playerState.duration
                        val newPosition = playerState.currentPosition
                        copy(
                            isPlaying = playerState.isPlaying,
                            duration = newDuration,
                            currentPosition = newPosition,
                            sliderPosition = if (newDuration > 0) newPosition.toFloat() / newDuration else 0f,
                            remainingTime = newDuration - newPosition,
                            formattedCurrentPosition = newPosition.formatTime(),
                            formattedRemainingTime = "-${(newDuration - newPosition).formatTime()}",
                        )
                    }

                    val currentState = _uiState.value
                    if (currentState is PlayerUiState.Success) {
                        val position = playerState.currentPosition
                        if (currentState.isPlaying && currentState.duration > 0 && position >= currentState.duration - 500) {
                            if (currentState.isRepeatOn) replayCurrentTrack() else nextTrack()
                        }
                    }
                }
            } catch (_: Exception) {
                _uiState.value = PlayerUiState.Error
            }
        }
    }

    private inline fun updateUiState(block: PlayerUiState.Success.() -> PlayerUiState.Success) {
        when (val currentState = _uiState.value) {
            PlayerUiState.Error, PlayerUiState.Loading -> _uiState.value = PlayerUiState.Success().block()
            is PlayerUiState.Success -> _uiState.value = currentState.block()
        }
    }

    fun togglePlayPause() {
        mediaPlayer.togglePlayPause()
    }

    fun seekTo(positionMs: Long) {
        mediaPlayer.seekTo(positionMs)
    }

    /**
     * Re-populates the playlist UI for [currentTrackId] without touching the player.
     * Used when navigating to the player screen for a track that is already loaded.
     */
    private fun refreshPlaylist(currentTrackId: Long, trackIds: List<Long>) {
        viewModelScope.launch {
            val allFlows = trackIds.map { getTrackByIdUseCase(it.toString()) }
            if (allFlows.isEmpty()) return@launch
            combine(*allFlows.toTypedArray()) { results -> results.toList().flatten() }
                .catch { _playlistUiState.value = PlaylistUiState.Error }
                .collect { allTracks ->
                    if (allTracks.isNotEmpty()) {
                        val orderByTrackId = trackIds.withIndex().associate { (index, id) -> id to index }
                        val orderedTracks = allTracks.sortedBy { track ->
                            orderByTrackId[track.trackId ?: Long.MIN_VALUE] ?: Int.MAX_VALUE
                        }

                        val currentTrack = orderedTracks.find { it.trackId == currentTrackId } ?: orderedTracks.first()
                        updateUiState { copy(currentTrack = currentTrack) }
                        _playlistUiState.value = PlaylistUiState.Success(orderedTracks)
                    }
                }
        }
    }

    fun playTrack(track: Track) {
        updateUiState { copy(currentTrack = track) }
        track.previewUrl?.let { url ->
            mediaPlayer.loadMedia(
                url = url,
                trackId = track.trackId,
                title = track.trackName,
                artist = track.artistName,
                artworkUri = track.artworkUrl100,
            )
            mediaPlayer.play()
        }
    }

    fun nextTrack() {
        val currentState = _uiState.value
        val playlistState = _playlistUiState.value
        if (currentState is PlayerUiState.Success && playlistState is PlaylistUiState.Success) {
            val current = currentState.currentTrack
            val currentIndex =
                playlistState.playableTracks.indexOfFirst { it.trackId == current?.trackId }
            if (currentIndex >= 0 && currentIndex < playlistState.playableTracks.size - 1) {
                playTrack(playlistState.playableTracks[currentIndex + 1])
            }
        }
    }

    fun previousTrack() {
        val currentState = _uiState.value
        val playlistState = _playlistUiState.value
        if (currentState is PlayerUiState.Success && playlistState is PlaylistUiState.Success) {
            val current = currentState.currentTrack
            val currentIndex =
                playlistState.playableTracks.indexOfFirst { it.trackId == current?.trackId }
            if (currentIndex > 0) {
                playTrack(playlistState.playableTracks[currentIndex - 1])
            }
        }
    }

    fun toggleRepeat() {
        updateUiState { copy(isRepeatOn = !isRepeatOn) }
    }

    fun replayCurrentTrack() {
        val currentState = _uiState.value
        if (currentState is PlayerUiState.Success) {
            currentState.currentTrack?.let { track ->
                playTrack(track)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Do NOT release the MediaPlayerWrapper here; playback should continue
        // in the background (visible via the mini player).
    }
}
