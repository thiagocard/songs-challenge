package com.songs.player.presentation.ui

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffold
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.rememberSupportingPaneScaffoldNavigator
import androidx.compose.material3.adaptive.separatingVerticalHingeBounds
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import com.songs.core.ui.util.isLargeScreen
import com.songs.player.domain.model.Track


@SuppressLint("SourceLockedOrientationActivity")
@Composable
internal fun PlayerScreen(
    currentTrackId: Long,
    uiState: PlayerUiState,
    playlistUiState: PlaylistUiState,
    snackbarHostState: SnackbarHostState,
    onNavigateUp: () -> Unit,
    onNavigateToAlbum: (String) -> Unit,
    onTrackClick: (Track) -> Unit,
    onPlayPauseClick: () -> Unit,
    onRewindClick: () -> Unit,
    onForwardClick: () -> Unit,
    onRepeatClick: () -> Unit,
    onSliderPositionChange: (Long) -> Unit,
) {
    val isLargeScreen = isLargeScreen()
    val windowAdaptiveInfo = currentWindowAdaptiveInfo()
    val isFoldableBookPosture = windowAdaptiveInfo.windowPosture.separatingVerticalHingeBounds.isNotEmpty()
    val showPlaylist = isLargeScreen || isFoldableBookPosture

    val activity = LocalActivity.current
    DisposableEffect(showPlaylist) {
        if (!showPlaylist) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    Screen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onNavigateUp = onNavigateUp,
        onNavigateToAlbum = onNavigateToAlbum,
        playlistUiState = playlistUiState,
        currentTrackId = currentTrackId,
        onTrackClick = onTrackClick,
        onPlayPauseClick = onPlayPauseClick,
        onRewindClick = onRewindClick,
        onForwardClick = onForwardClick,
        onRepeatClick = onRepeatClick,
        onSliderPositionChange = onSliderPositionChange,
        showPlaylist = showPlaylist,
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun Screen(
    uiState: PlayerUiState,
    snackbarHostState: SnackbarHostState,
    onNavigateUp: () -> Unit,
    onNavigateToAlbum: (String) -> Unit,
    playlistUiState: PlaylistUiState,
    currentTrackId: Long,
    onTrackClick: (Track) -> Unit,
    onPlayPauseClick: () -> Unit,
    onRewindClick: () -> Unit,
    onForwardClick: () -> Unit,
    onRepeatClick: () -> Unit,
    onSliderPositionChange: (Long) -> Unit,
    showPlaylist: Boolean,
) {
    val windowAdaptiveInfo = currentWindowAdaptiveInfo()
    val directive = if (showPlaylist) {
        calculatePaneScaffoldDirective(windowAdaptiveInfo)
    } else {
        PaneScaffoldDirective.Default
    }
    val navigator = rememberSupportingPaneScaffoldNavigator(scaffoldDirective = directive)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        SupportingPaneScaffold(
            directive = navigator.scaffoldDirective,
            value = navigator.scaffoldValue,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues),
            mainPane = {
                AnimatedPane {
                    Player(
                        uiState = uiState,
                        currentTrackId = currentTrackId,
                        onNavigateUp = onNavigateUp,
                        onNavigateToAlbum = onNavigateToAlbum,
                        onPlayPauseClick = onPlayPauseClick,
                        onRewindClick = onRewindClick,
                        onForwardClick = onForwardClick,
                        onRepeatClick = onRepeatClick,
                        modifier = Modifier.fillMaxSize(),
                        onSliderPositionChange = onSliderPositionChange,
                    )
                }
            },
            supportingPane = {
                AnimatedPane {
                    Playlist(
                        modifier = Modifier.fillMaxSize(),
                        playlistUiState = playlistUiState,
                        uiState = uiState,
                        onTrackClick = onTrackClick,
                    )
                }
            },
        )
    }
}
