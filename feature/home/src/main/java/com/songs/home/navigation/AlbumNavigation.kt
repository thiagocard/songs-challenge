package com.songs.home.navigation

import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.songs.home.presentation.ui.album.AlbumScreen
import com.songs.home.presentation.ui.album.AlbumViewModel
import com.songs.navigation.route.AlbumRoute

fun EntryProviderScope<NavKey>.albumRoute(
    onNavigateUp: () -> Unit,
    onNavigateToPlayer: (trackIds: List<Long>, currentTrackId: Long) -> Unit,
) {
    entry<AlbumRoute>(
        clazzContentKey = { AlbumRoute.Key },
    ) { route ->
        val viewModel = hiltViewModel<AlbumViewModel, AlbumViewModel.Factory>(
            creationCallback = { factory -> factory.create(route.albumId) },
            key = route.albumId,
        )
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        AlbumScreen(
            uiState = uiState,
            onNavigateUp = onNavigateUp,
            onNavigateToPlayer = onNavigateToPlayer
        )
    }
}
