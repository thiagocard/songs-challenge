package com.songs.player.presentation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.songs.common.extension.formatTime
import com.songs.common.playback.NowPlayingProvider
import com.songs.player.domain.model.Track
import com.songs.player.domain.usecase.GetTrackByIdUseCase
import com.songs.player.media.MediaPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class PlayerViewModel @Inject constructor(
    private val getTrackByIdUseCase: GetTrackByIdUseCase,
    private val mediaPlayer: MediaPlayer,
    private val nowPlayingProvider: NowPlayingProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow<PlayerUiState>(PlayerUiState.Loading)
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    val playbackError: SharedFlow<Unit> = mediaPlayer.playbackErrors

    private val _events = MutableSharedFlow<PlayerSideEffect>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    private val _playlistUiState = MutableStateFlow<PlaylistUiState>(PlaylistUiState.Loading)
    val playlistUiState: StateFlow<PlaylistUiState> = _playlistUiState.asStateFlow()

    /**
     * Set by [requestPlay] when the user explicitly taps a song to start playback.
     * [loadTrack] will only call [playTrack] when this matches the requested track,
     * ensuring that back-navigation to the PlayerScreen never restarts audio.
     */
    private var pendingPlayTrackId: Long? = null

    private var nowPlayingTrackId: Long? = null

    init {
        observeMediaPlayer()
        observeNowPlaying()
    }

    /**
     * Called by the UI when the user **explicitly** navigates to a track with intent to play.
     * Must be called before [loadTrack] for the same [trackId].
     */
    fun requestPlay(trackId: Long) {
        if (trackId == 0L) return
        pendingPlayTrackId = trackId
    }

    fun onPlaylistTrackClick(track: Track) {
        val currentTrackId = track.trackId ?: return
        val currentPlaylistIds = when (val state = _playlistUiState.value) {
            is PlaylistUiState.Success -> state.playableTracks.mapNotNull { it.trackId }
            PlaylistUiState.Error, PlaylistUiState.Loading -> emptyList()
        }

        val trackIds = currentPlaylistIds
            .ifEmpty { listOf(currentTrackId) }

        viewModelScope.launch {
            _events.emit(PlayerSideEffect.NavigateToPlayer(trackIds, currentTrackId))
        }
    }

    fun loadTrack(currentTrackId: Long, trackIds: List<Long>) {
        val resolvedTrackIds = resolveTrackIds(currentTrackId, trackIds)
        val alreadyLoaded = nowPlayingTrackId == currentTrackId
        if (alreadyLoaded) {
            // Ensure pending play intents never "leak" across navigations when the track is already loaded.
            pendingPlayTrackId = null
            refreshPlaylist(currentTrackId, resolvedTrackIds)
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
                val otherTracksFlows = resolvedTrackIds
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
                                if (shouldPlay) playTrack(currentTrack, resolvedTrackIds)
                                // If we don't have a playlist context (deeplink/notification),
                                // keep at least the current track so player controls behave consistently.
                                _playlistUiState.value = PlaylistUiState.Success(listOf(currentTrack))
                            } else {
                                _playlistUiState.value = PlaylistUiState.Error
                            }
                        }
                } else {
                    combine(
                        currentTrackFlow,
                        *otherTracksFlows.toTypedArray()
                    ) { results ->
                        results.toSet().flatten()
                    }
                        .catch { _playlistUiState.value = PlaylistUiState.Error }
                        .collect { allTracks ->
                            if (allTracks.isNotEmpty()) {
                                val orderByTrackId =
                                    resolvedTrackIds.withIndex().associate { (index, id) -> id to index }
                                val orderedTracks = allTracks.sortedBy { track ->
                                    orderByTrackId[track.trackId ?: Long.MIN_VALUE] ?: Int.MAX_VALUE
                                }

                                val currentTrack = orderedTracks.find { it.trackId == currentTrackId }
                                    ?: orderedTracks.first()
                                updateUiState { copy(currentTrack = currentTrack) }
                                if (shouldPlay) playTrack(currentTrack, resolvedTrackIds)
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

    private fun observeNowPlaying() {
        viewModelScope.launch {
            nowPlayingProvider.nowPlaying.collect { nowPlaying ->
                nowPlayingTrackId = nowPlaying?.trackId
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

    private fun resolveTrackIds(currentTrackId: Long, trackIds: List<Long>): List<Long> {
        if (trackIds.size > 1) return trackIds

        // If we already have a playlist (e.g. coming back from a shared-element transition),
        // prefer it over losing next/previous navigation.
        val existing = (_playlistUiState.value as? PlaylistUiState.Success)
            ?.playableTracks
            ?.mapNotNull { it.trackId }
            .orEmpty()

        return when {
            existing.size > 1 && existing.contains(currentTrackId) -> existing
            trackIds.isNotEmpty() -> trackIds
            else -> listOf(currentTrackId)
        }
    }

    fun playTrack(track: Track, trackIds: List<Long> = emptyList()) {
        updateUiState { copy(currentTrack = track) }
        
        // If trackIds is empty, try to get it from current playlist state
        val playlistTrackIds = trackIds.ifEmpty {
            (_playlistUiState.value as? PlaylistUiState.Success)
                ?.playableTracks
                ?.mapNotNull { it.trackId }
                .orEmpty()
        }
        
        track.previewUrl?.let { url ->
            mediaPlayer.loadMedia(
                url = url,
                trackId = track.trackId,
                title = track.trackName,
                artist = track.artistName,
                artworkUri = track.artworkUrl100,
                trackIds = playlistTrackIds,
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
