package com.songs.player.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.compose.material3.SnackbarHostState
import com.songs.feature.player.R
import com.songs.navigation.route.PlayerRoute
import com.songs.player.presentation.ui.PlayerScreen
import com.songs.player.presentation.ui.PlayerViewModel
import kotlinx.coroutines.flow.collectLatest

fun EntryProviderScope<NavKey>.playerScreen(
    onNavigateUp: () -> Unit,
    onNavigateToAlbum: (String) -> Unit,
    onRouteChanged: (trackIds: List<Long>, currentTrackId: Long) -> Unit,
) {
    entry<PlayerRoute>(clazzContentKey = { PlayerRoute.Key }) { route ->
        val viewModel: PlayerViewModel = hiltViewModel()
        val snackbarHostState = remember { SnackbarHostState() }
        val playbackErrorMessage = stringResource(R.string.error_playback)

        LaunchedEffect(route.trackIds, route.currentTrackId) {
            onRouteChanged(route.trackIds, route.currentTrackId)
            if (route.shouldPlay) {
                viewModel.requestPlay(route.currentTrackId)
            }
            viewModel.loadTrack(route.currentTrackId, route.trackIds)
        }

        LaunchedEffect(Unit) {
            viewModel.playbackError.collectLatest {
                snackbarHostState.showSnackbar(message = playbackErrorMessage)
            }
        }

        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val playlistUiState by viewModel.playlistUiState.collectAsStateWithLifecycle()

        PlayerScreen(
            currentTrackId = route.currentTrackId,
            uiState = uiState,
            playlistUiState = playlistUiState,
            snackbarHostState = snackbarHostState,
            onNavigateUp = onNavigateUp,
            onNavigateToAlbum = onNavigateToAlbum,
            onTrackClick = viewModel::playTrack,
            onPlayPauseClick = viewModel::togglePlayPause,
            onRewindClick = viewModel::previousTrack,
            onForwardClick = viewModel::nextTrack,
            onRepeatClick = viewModel::toggleRepeat,
            onSliderPositionChange = viewModel::seekTo,
        )
    }
}
