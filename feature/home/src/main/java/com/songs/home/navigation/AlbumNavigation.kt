package com.songs.home.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.metadata
import androidx.navigation3.ui.NavDisplay
import com.songs.home.presentation.ui.album.AlbumScreen
import com.songs.home.presentation.ui.album.AlbumViewModel
import com.songs.navigation.route.AlbumRoute

fun EntryProviderScope<NavKey>.albumScreen(
    onNavigateUp: () -> Unit,
    onNavigateToPlayer: (trackIds: List<Long>, currentTrackId: Long) -> Unit,
) {
    entry<AlbumRoute>(
        clazzContentKey = { AlbumRoute.Key },
        metadata = metadata {
            put(NavDisplay.TransitionKey) { albumScreenTransition }
            put(NavDisplay.PopTransitionKey) { albumScreenPopTransition }
        },
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

private val albumScreenTransition = slideInHorizontally(tween(400)) { it } togetherWith
    slideOutHorizontally(tween(600)) { -it / 3 }

private val albumScreenPopTransition = slideInHorizontally(tween(400)) { -it / 3 } togetherWith
    slideOutHorizontally(tween(600)) { it }
