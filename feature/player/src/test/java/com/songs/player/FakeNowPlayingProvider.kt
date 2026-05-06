package com.songs.player

import com.songs.common.playback.NowPlaying
import com.songs.common.playback.NowPlayingProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeNowPlayingProvider(
    initialNowPlaying: NowPlaying? = null,
) : NowPlayingProvider {
    private val _nowPlaying = MutableStateFlow<NowPlaying?>(initialNowPlaying)
    override val nowPlaying: Flow<NowPlaying?> = _nowPlaying

    fun emit(nowPlaying: NowPlaying?) {
        _nowPlaying.value = nowPlaying
    }
}

