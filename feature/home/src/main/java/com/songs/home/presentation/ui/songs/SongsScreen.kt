package com.songs.home.presentation.ui.songs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.compose.animation.ExperimentalSharedTransitionApi
import com.songs.home.domain.model.Song
import com.songs.core.ui.components.MediaDropdownMenuItemAction
import com.songs.core.ui.components.SongItemComponent
import com.songs.core.ui.screen.LoadingScreen
import com.songs.core.ui.transition.LocalSharedTransitionScope
import com.songs.feature.home.R
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.songs.core.ui.util.isLargeScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SongsScreen(
    searchTerm: String,
    pagingItems: LazyPagingItems<Song>,
    onSearchTermChanged: (String) -> Unit,
    onNavigateToAlbum: (String) -> Unit,
    onSongClick: (Song) -> Unit,
    nowPlayingTrackId: Long? = null,
) {
    var searchTextFieldValue by remember { mutableStateOf(TextFieldValue(searchTerm)) }
    var isSearchActive by remember { mutableStateOf(false) }


    val isRefreshing =
        pagingItems.loadState.refresh is LoadState.Loading && pagingItems.itemCount > 0
    val isInitialLoading =
        pagingItems.loadState.refresh is LoadState.Loading && pagingItems.itemCount == 0
    val isError = pagingItems.loadState.refresh is LoadState.Error && pagingItems.itemCount == 0
    val isAppendLoading = pagingItems.loadState.append is LoadState.Loading

    var showHeader by remember { mutableStateOf(true) }
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    // When returning from the player, ensure the currently-playing row is composed so the
    // reverse shared-element transition can target it (even if the user changed tracks).
    LaunchedEffect(nowPlayingTrackId, isSearchActive, pagingItems.itemCount) {
        val trackId = nowPlayingTrackId ?: return@LaunchedEffect
        if (isSearchActive) return@LaunchedEffect

        // Paging may still be loading; only scroll when we can resolve an index.
        val index = pagingItems.itemSnapshotList.items.indexOfFirst { it?.trackId == trackId }
        if (index >= 0) {
            withContext(Dispatchers.Main.immediate) {
                listState.scrollToItem(index)
            }
        }
    }

    // Show append errors (load-more failures) as a snackbar.
    LaunchedEffect(pagingItems.loadState) {
        val appendError = pagingItems.loadState.append as? LoadState.Error ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(
            message = appendError.error.message ?: "Couldn't load more.",
            duration = SnackbarDuration.Short,
        )
    }

    // Track scroll direction to show/hide header.
    var previousFirstVisibleIndex by remember { mutableIntStateOf(0) }
    var previousFirstVisibleOffset by remember { mutableIntStateOf(0) }

    LaunchedEffect(listState) {
        snapshotFlow {
            listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
        }.distinctUntilChanged()
            .collectLatest { (index, offset) ->
                val scrolledDown = index > previousFirstVisibleIndex
                        || (index == previousFirstVisibleIndex && offset > previousFirstVisibleOffset)
                val scrolledUp = index < previousFirstVisibleIndex
                        || (index == previousFirstVisibleIndex && offset < previousFirstVisibleOffset)
                if (scrolledDown) showHeader = false
                if (scrolledUp) showHeader = true
                previousFirstVisibleIndex = index
                previousFirstVisibleOffset = offset
            }
    }

    val isLargeScreen = isLargeScreen()
    val largeScreenModifier = if (isLargeScreen) Modifier.padding(top = 24.dp) else Modifier

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier
            .consumeWindowInsets(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
            .then(largeScreenModifier)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Header(
                isVisible = showHeader,
                isSearchActive = isSearchActive,
                onSearchActiveChanged = { isSearchActive = it },
                searchTextFieldValue = searchTextFieldValue,
                searchTextFieldValueChange = { searchTextFieldValue = it },
                onSearchTermChanged = onSearchTermChanged,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))

            when {
                isInitialLoading -> LoadingScreen()

                isError -> Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = stringResource(R.string.error_loading_songs),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                else -> {
                    val pullState = rememberPullToRefreshState()

                    val songsList = @Composable {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            items(
                                count = pagingItems.itemCount,
                                // key = ** do NOT set a key here - iTunes API returns duplicates //
                            ) { index ->
                                val song = pagingItems[index] ?: return@items
                                val isPreview = LocalInspectionMode.current
                                val sharedTransitionScope = LocalSharedTransitionScope.current
                                val animatedContentScope = if (!isPreview) LocalNavAnimatedContentScope.current else null
                                @OptIn(ExperimentalSharedTransitionApi::class)
                                val artworkModifier = if (sharedTransitionScope != null && animatedContentScope != null)
                                    with(sharedTransitionScope) {
                                        Modifier.sharedElement(
                                            sharedContentState = rememberSharedContentState(key = "song_artwork_${song.trackId}"),
                                            animatedVisibilityScope = animatedContentScope,
                                        )
                                    } else Modifier
                                @OptIn(ExperimentalSharedTransitionApi::class)
                                val titleModifier = if (sharedTransitionScope != null && animatedContentScope != null)
                                    with(sharedTransitionScope) {
                                        Modifier.sharedElement(
                                            sharedContentState = rememberSharedContentState(key = "song_title_${song.trackId}"),
                                            animatedVisibilityScope = animatedContentScope,
                                        )
                                    } else Modifier
                                @OptIn(ExperimentalSharedTransitionApi::class)
                                val subtitleModifier = if (sharedTransitionScope != null && animatedContentScope != null)
                                    with(sharedTransitionScope) {
                                        Modifier.sharedElement(
                                            sharedContentState = rememberSharedContentState(key = "song_artist_${song.trackId}"),
                                            animatedVisibilityScope = animatedContentScope,
                                        )
                                    } else Modifier
                                SongItemComponent(
                                    title = song.trackName,
                                    subtitle = song.artistName,
                                    artworkUrl = song.artworkUrl100,
                                    modifier = Modifier.animateItem(),
                                    artworkModifier = artworkModifier,
                                    titleModifier = titleModifier,
                                    subtitleModifier = subtitleModifier,
                                    onMenuClick = { action ->
                                        when (action) {
                                            MediaDropdownMenuItemAction.VIEW_ALBUM -> {
                                                song.collectionId?.let { collectionId ->
                                                    onNavigateToAlbum(collectionId.toString())
                                                }
                                            }
                                        }

                                    },
                                    onClick = { onSongClick(song) }
                                )
                            }
                            if (isAppendLoading) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 16.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }
                        }
                    }

                    if (isSearchActive) {
                        songsList()
                    } else {
                        PullToRefreshBox(
                            state = pullState,
                            isRefreshing = isRefreshing,
                            onRefresh = { pagingItems.refresh() },
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.TopCenter,
                        ) {
                            songsList()
                        }
                    }
                }
            }
        }
    }
}
