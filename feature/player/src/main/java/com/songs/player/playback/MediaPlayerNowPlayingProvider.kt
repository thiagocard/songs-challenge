package com.songs.player.playback

import com.songs.common.playback.NowPlaying
import com.songs.common.playback.NowPlayingProvider
import com.songs.player.media.MediaPlayer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class MediaPlayerNowPlayingProvider @Inject constructor(
    mediaPlayer: MediaPlayer,
) : NowPlayingProvider {
    override val nowPlaying: Flow<NowPlaying?> =
        mediaPlayer.state
            .map { state ->
                val trackId = state.currentTrackId
                if (trackId != null) {
                    NowPlaying(
                        trackId = trackId,
                        title = state.trackTitle.orEmpty(),
                        artistName = state.artistName.orEmpty(),
                        artworkUrl = state.artworkUrl,
                        trackIds = state.trackIds,
                    )
                } else {
                    null
                }
            }
            .distinctUntilChanged()
}
