package com.songs.player.presentation.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.songs.core.ui.components.AlbumCover
import com.songs.core.ui.components.AppHeader
import com.songs.core.ui.components.NavigationIcon
import com.songs.core.ui.screen.LoadingScreen
import com.songs.core.ui.theme.PhonePreviews
import com.songs.core.ui.theme.TabletPreviews
import com.songs.core.ui.theme.ThemePreview
import com.songs.core.ui.transition.LocalSharedTransitionScope
import com.songs.feature.player.R
import com.songs.player.domain.model.Track

@Composable
internal fun Player(
    uiState: PlayerUiState,
    currentTrackId: Long,
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit,
    onNavigateToAlbum: (String) -> Unit,
    onPlayPauseClick: () -> Unit,
    onRewindClick: () -> Unit,
    onForwardClick: () -> Unit,
    onRepeatClick: () -> Unit,
    onSliderPositionChange: (Long) -> Unit,
) {
    when (uiState) {
        is PlayerUiState.Error -> {
            Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.error_loading_song),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        is PlayerUiState.Loading -> {
            LoadingScreen()
        }

        is PlayerUiState.Success -> {
            val sharedTrackId = uiState.currentTrack?.trackId ?: currentTrackId
            InternalPlayer(
                modifier = modifier,
                onNavigateUp = onNavigateUp,
                onNavigateToAlbum = onNavigateToAlbum,
                uiState = uiState,
                currentTrackId = sharedTrackId,
                onSliderPositionChange = onSliderPositionChange,
                onPlayPauseClick = onPlayPauseClick,
                onRewindClick = onRewindClick,
                onForwardClick = onForwardClick,
                onRepeatClick = onRepeatClick,
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun InternalPlayer(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit,
    onNavigateToAlbum: (String) -> Unit,
    uiState: PlayerUiState.Success,
    currentTrackId: Long,
    onSliderPositionChange: (Long) -> Unit,
    onPlayPauseClick: () -> Unit,
    onRewindClick: () -> Unit,
    onForwardClick: () -> Unit,
    onRepeatClick: () -> Unit,
) {
    val isPreview = LocalInspectionMode.current
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedContentScope = if (!isPreview) LocalNavAnimatedContentScope.current else null
    val useSharedElements = !isPreview && sharedTransitionScope != null && animatedContentScope != null

    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Header(
            onNavigateUp = onNavigateUp,
            onNavigateToAlbum = onNavigateToAlbum,
            currentTrack = uiState.currentTrack
        )

        Spacer(Modifier.weight(1f))

        Box(
            modifier = Modifier
                .size(286.dp)
                .align(Alignment.CenterHorizontally),
        ){
            AlbumCover(
                imageUrl = uiState.currentTrack?.artworkUrl500,
                modifier = Modifier
                    .size(286.dp)
                    .then(
                        if (useSharedElements) {
                            with(sharedTransitionScope) {
                                Modifier
                                    .sharedElement(
                                        sharedContentState = rememberSharedContentState(key = "song_artwork_$currentTrackId"),
                                        animatedVisibilityScope = animatedContentScope,
                                    )
                                    .skipToLookaheadSize()
                            }
                        } else Modifier
                    )
            )
        }
        Spacer(Modifier.weight(1f))

        AlbumInfo(
            currentTrack = uiState.currentTrack,
            titleModifier = if (useSharedElements) {
                with(sharedTransitionScope) {
                    Modifier
                        .sharedElement(
                            sharedContentState = rememberSharedContentState(key = "song_title_$currentTrackId"),
                            animatedVisibilityScope = animatedContentScope,
                        )
                        .skipToLookaheadSize()
                }
            } else Modifier,
            subtitleModifier = if (useSharedElements) {
                with(sharedTransitionScope) {
                    Modifier
                        .sharedElement(
                            sharedContentState = rememberSharedContentState(key = "song_artist_$currentTrackId"),
                            animatedVisibilityScope = animatedContentScope,
                        )
                        .skipToLookaheadSize()
                }
            } else Modifier,
        )


        SongProgressTracker(
            sliderPosition = uiState.sliderPosition,
            onSliderPositionChange = onSliderPositionChange,
            duration = uiState.duration,
            formattedCurrentPosition = uiState.formattedCurrentPosition,
            formattedRemainingTime = uiState.formattedRemainingTime,
        )

        PlayerControls(
            isPlaying = uiState.isPlaying,
            isRepeatOn = uiState.isRepeatOn,
            onPlayPauseClick = onPlayPauseClick,
            onRewindClick = onRewindClick,
            onForwardClick = onForwardClick,
            onRepeatClick = onRepeatClick,
        )
    }
}

@Composable
private fun Header(
    onNavigateUp: () -> Unit,
    onNavigateToAlbum: (String) -> Unit,
    currentTrack: Track?
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    AppHeader(
        title = stringResource(R.string.now_playing),
        navigationIcon = { NavigationIcon(onNavigateUp = onNavigateUp) },
        trailingContent = {
            IconButton(onClick = { isMenuExpanded = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.menu),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            DropdownMenu(
                expanded = isMenuExpanded,
                onDismissRequest = { isMenuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.view_album),
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = stringResource(R.string.chevron),
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    onClick = {
                        currentTrack?.collectionId?.let { collectionId ->
                            onNavigateToAlbum(collectionId.toString())
                        }
                        isMenuExpanded = false
                    }
                )
            }
        }
    )
}

@PhonePreviews
@TabletPreviews
@Composable
private fun Preview(
    @PreviewParameter(TrackPreviewParameterProvider::class, limit = 1) track: Track
) {
    ThemePreview {
        Player(
                uiState = PlayerUiState.Success(
                    currentTrack = track,
                    isPlaying = true,
                    isRepeatOn = false,
                    currentPosition = 90000L,
                    duration = track.trackTimeMillis ?: 240000L
                ),
                currentTrackId = 0L,
                onNavigateUp = {},
                onNavigateToAlbum = {},
                onPlayPauseClick = {},
                onRewindClick = {},
                onForwardClick = {},
                onRepeatClick = {},
                onSliderPositionChange = {}
            )
    }
}
