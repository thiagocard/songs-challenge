package com.songs.player.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.songs.core.ui.components.SongItemComponent
import com.songs.core.ui.theme.PhonePreviews
import com.songs.core.ui.theme.TabletPreviews
import com.songs.core.ui.theme.ThemePreview
import com.songs.feature.player.R
import com.songs.player.domain.model.Track

@Composable
internal fun Playlist(
    modifier: Modifier = Modifier,
    playlistUiState: PlaylistUiState,
    uiState: PlayerUiState,
    onTrackClick: (Track) -> Unit,
) {
    when (playlistUiState) {
        is PlaylistUiState.Success -> {
            if (uiState is PlayerUiState.Success) {
                Playlist(
                    modifier = modifier,
                    playableTracks = playlistUiState.playableTracks,
                    currentTrack = uiState.currentTrack,
                    onTrackClick = onTrackClick,
                )
            } else {
                Loading()
            }
        }

        is PlaylistUiState.Loading -> {
            Loading()
        }

        is PlaylistUiState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Error loading playlist",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun Loading() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun Playlist(
    modifier: Modifier = Modifier,
    playableTracks: List<Track>,
    currentTrack: Track?,
    onTrackClick: (Track) -> Unit,
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_music_list),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(16.dp)
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(playableTracks) { track ->
                SongItemComponent(
                    title = track.trackName.orEmpty(),
                    subtitle = track.artistName.orEmpty(),
                    artworkUrl = track.artworkUrl100,
                    showNavOption = false,
                    isPlaying = track.trackId == currentTrack?.trackId,
                    onClick = { onTrackClick(track) }
                )
            }
        }
    }
}

@PhonePreviews
@TabletPreviews
@Composable
private fun PlaylistPreview(
    @PreviewParameter(TrackPreviewParameterProvider::class) track: Track
) {
    val tracks = listOf(
        track,
        track.copy(trackId = (track.trackId ?: 0) + 1, trackName = "Another Song"),
        track.copy(trackId = (track.trackId ?: 0) + 2, trackName = "Yet Another Song"),
    )
    ThemePreview {
        Row {
            Playlist(
                playlistUiState = PlaylistUiState.Success(playableTracks = tracks),
                uiState = PlayerUiState.Success(
                    currentTrack = track,
                    isPlaying = true,
                    isRepeatOn = false,
                    currentPosition = 90000L,
                    duration = track.trackTimeMillis ?: 240000L
                ),
                onTrackClick = {}
            )
        }
    }
}
