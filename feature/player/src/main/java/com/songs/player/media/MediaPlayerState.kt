package com.songs.player.media

data class MediaPlayerState(
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val trackTitle: String? = null,
    val artistName: String? = null,
    val artworkUrl: String? = null,
    /** The trackId of the media item currently loaded into the player, or null if nothing is loaded. */
    val currentTrackId: Long? = null,
    val trackIds: List<Long> = emptyList(),
)
