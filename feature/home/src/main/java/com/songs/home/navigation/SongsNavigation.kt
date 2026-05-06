package com.songs.home.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.paging.compose.collectAsLazyPagingItems
import com.songs.home.presentation.ui.songs.SongsSideEffect
import com.songs.home.presentation.ui.songs.SongsScreen
import com.songs.home.presentation.ui.songs.SongsViewModel
import com.songs.navigation.route.HomeRoute
import kotlinx.coroutines.flow.collectLatest

fun EntryProviderScope<NavKey>.songsRoute(
    onNavigateToAlbum: (String) -> Unit,
    onNavigateToPlayer: (trackIds: List<Long>, currentTrackId: Long) -> Unit,
) {
    entry<HomeRoute> {
        val viewModel: SongsViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val pagingItems = viewModel.pagingData.collectAsLazyPagingItems()

        LaunchedEffect(Unit) {
            viewModel.events.collectLatest { event ->
                when (event) {
                    is SongsSideEffect.NavigateToPlayer -> {
                        onNavigateToPlayer(event.trackIds, event.currentTrackId)
                    }
                }
            }
        }

        SongsScreen(
            searchTerm = uiState.searchTerm,
            pagingItems = pagingItems,
            onSearchTermChanged = viewModel::onSearchTermChanged,
            onResetToDefault = viewModel::resetToDefault,
            onNavigateToAlbum = onNavigateToAlbum,
            onSongClick = { song -> viewModel.onSongClick(song, pagingItems) },
            nowPlayingTrackId = uiState.nowPlayingTrackId,
        )
    }
}
