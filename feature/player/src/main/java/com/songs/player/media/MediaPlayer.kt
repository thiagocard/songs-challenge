package com.songs.player.media

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface MediaPlayer {
    val state: StateFlow<MediaPlayerState>
    val playbackErrors: SharedFlow<Unit>

    fun loadMedia(
        url: String,
        trackId: Long? = null,
        title: String? = null,
        artist: String? = null,
        artworkUri: String? = null,
        trackIds: List<Long> = emptyList(),
    )

    fun play()
    fun pause()
    fun seekTo(positionMs: Long)
    fun togglePlayPause()
    fun release()
}
