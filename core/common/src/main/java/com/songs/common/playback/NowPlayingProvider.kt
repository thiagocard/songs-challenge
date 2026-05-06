package com.songs.common.playback

import kotlinx.coroutines.flow.Flow

data class NowPlaying(
    val trackId: Long,
    val title: String,
    val artistName: String,
    val artworkUrl: String?,
    val trackIds: List<Long>
)

/**
 * Exposes the currently loaded/playing track data (if any).
 *
 * This lives in core so feature modules can observe "now playing" without depending on
 * the player implementation details.
 */
interface NowPlayingProvider {
    val nowPlaying: Flow<NowPlaying?>
}
