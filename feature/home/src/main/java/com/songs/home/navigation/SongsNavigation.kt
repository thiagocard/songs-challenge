package com.songs.home.navigation

import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.paging.compose.collectAsLazyPagingItems
import com.songs.home.presentation.ui.songs.SongsScreen
import com.songs.home.presentation.ui.songs.SongsViewModel
import com.songs.navigation.route.HomeRoute

fun EntryProviderScope<NavKey>.songsScreen(
    onNavigateToAlbum: (String) -> Unit,
    onNavigateToPlayer: (trackIds: List<Long>, currentTrackId: Long) -> Unit,
) {
    entry<HomeRoute> {
        val viewModel: SongsViewModel = hiltViewModel()
        val searchTerm by viewModel.searchTerm.collectAsStateWithLifecycle()
        val pagingItems = viewModel.pagingData.collectAsLazyPagingItems()

        SongsScreen(
            searchTerm,
            pagingItems,
            viewModel::onSearchTermChanged,
            viewModel::resetToDefault,
            onNavigateToAlbum,
            onNavigateToPlayer,
        )
    }
}
