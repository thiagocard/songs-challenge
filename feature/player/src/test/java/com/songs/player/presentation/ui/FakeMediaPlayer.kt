package com.songs.player.presentation.ui

import com.songs.player.media.MediaPlayer
import com.songs.player.media.MediaPlayerState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

class FakeMediaPlayer : MediaPlayer {

    private val _state = MutableStateFlow(MediaPlayerState())
    override val state: StateFlow<MediaPlayerState> = _state

    private val _playbackErrors = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val playbackErrors: SharedFlow<Unit> = _playbackErrors

    var loadMediaCalled = false
    var playCalled = false
    var togglePlayPauseCalled = false
    var lastLoadedUrl: String? = null
    var lastLoadedTrackId: Long? = null

    fun emitState(newState: MediaPlayerState) {
        _state.value = newState
    }

    fun emitPlaybackError() {
        _playbackErrors.tryEmit(Unit)
    }

    fun resetCalls() {
        loadMediaCalled = false
        playCalled = false
        togglePlayPauseCalled = false
        lastLoadedUrl = null
        lastLoadedTrackId = null
    }

    override fun loadMedia(url: String, trackId: Long?, title: String?, artist: String?, artworkUri: String?) {
        loadMediaCalled = true
        lastLoadedUrl = url
        lastLoadedTrackId = trackId
        _state.value = _state.value.copy(currentTrackId = trackId)
    }

    override fun play() {
        playCalled = true
    }

    override fun pause() {}

    override fun seekTo(positionMs: Long) {
        _state.value = _state.value.copy(currentPosition = positionMs)
    }

    override fun togglePlayPause() {
        togglePlayPauseCalled = true
    }

    override fun release() {}
}
