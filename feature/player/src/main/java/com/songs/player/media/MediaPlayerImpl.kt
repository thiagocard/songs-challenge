package com.songs.player.media

import android.content.ComponentName
import android.content.Context
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.songs.common.coroutine.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MediaPlayerImpl(
    private val context: Context,
    dispatcherProvider: DispatcherProvider,
) : MediaPlayer {

    private val _state = MutableStateFlow(MediaPlayerState())
    override val state: StateFlow<MediaPlayerState> = _state

    private val _playbackErrors = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val playbackErrors: SharedFlow<Unit> = _playbackErrors

    private var mediaController: MediaController? = null
    private var positionUpdateJob: Job? = null
    private val scope = CoroutineScope(dispatcherProvider.main + SupervisorJob())

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _state.value = _state.value.copy(isPlaying = isPlaying)
            if (isPlaying) startPositionUpdates() else stopPositionUpdates()
        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            _state.value = _state.value.copy(currentPosition = newPosition.positionMs)
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_READY) {
                _state.value = _state.value.copy(duration = mediaController?.duration ?: 0L)
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            _playbackErrors.tryEmit(Unit)
        }
    }

    init {
        connectToService()
    }

    private fun connectToService() {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, PlaybackService::class.java)
        )
        val future = MediaController.Builder(context, sessionToken).buildAsync()
        future.addListener(
            {
                mediaController = future.get().apply {
                    addListener(playerListener)
                }
            },
            context.mainExecutor
        )
    }

    private fun startPositionUpdates() {
        stopPositionUpdates()
        positionUpdateJob = scope.launch {
            while (true) {
                _state.value = _state.value.copy(currentPosition = mediaController?.currentPosition ?: 0L)
                delay(100)
            }
        }
    }

    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }

    override fun loadMedia(
        url: String,
        trackId: Long?,
        title: String?,
        artist: String?,
        artworkUri: String?,
        trackIds: List<Long>,
    ) {
        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .apply {
                artworkUri?.let { setArtworkUri(it.toUri()) }
            }
            .build()
        val mediaItem = MediaItem.Builder()
            .apply { trackId?.toString()?.let(::setMediaId) }
            .setUri(url)
            .setMediaMetadata(metadata)
            .build()
        mediaController?.setMediaItem(mediaItem)
        mediaController?.prepare()
        _state.value = _state.value.copy(
            trackTitle = title,
            artistName = artist,
            artworkUrl = artworkUri,
            currentTrackId = trackId,
            trackIds = trackIds,
        )
    }

    override fun play() {
        mediaController?.play()
    }

    override fun pause() {
        mediaController?.pause()
    }

    override fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs)
        _state.value = _state.value.copy(currentPosition = positionMs)
    }

    override fun togglePlayPause() {
        if (_state.value.isPlaying) pause() else play()
    }

    override fun release() {
        stopPositionUpdates()
        mediaController?.removeListener(playerListener)
        mediaController?.release()
        mediaController = null
    }
}
