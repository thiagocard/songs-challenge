package com.songs.home.presentation.ui.album

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.songs.core.ui.components.AlbumItemComponent
import com.songs.core.ui.components.AppHeader
import com.songs.core.ui.components.MediaItemComponentStyle
import com.songs.core.ui.components.NavigationIcon
import com.songs.core.ui.components.SongItemComponent
import com.songs.core.ui.screen.LoadingScreen
import com.songs.core.ui.theme.PhonePreviews
import com.songs.core.ui.theme.TabletPreviews
import com.songs.core.ui.theme.ThemePreview
import com.songs.core.ui.transition.LocalSharedTransitionScope
import com.songs.core.ui.util.isLargeScreen
import com.songs.feature.home.R
import com.songs.home.domain.model.AlbumWithSongs

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AlbumScreen(
    uiState: AlbumUiState,
    onNavigateUp: () -> Unit,
    onNavigateToPlayer: (trackIds: List<Long>, currentTrackId: Long) -> Unit,
) {
    Scaffold(
        modifier = Modifier
            .consumeWindowInsets(
                WindowInsets.systemBars.only(WindowInsetsSides.Bottom)
            )
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is AlbumUiState.Loading -> {
                    LoadingScreen()
                }

                is AlbumUiState.Success -> {
                    Screen(
                        onNavigateUp = onNavigateUp,
                        onNavigateToPlayer = onNavigateToPlayer,
                        state = uiState
                    )
                }

                is AlbumUiState.Error -> {
                    Text(
                        text = stringResource(R.string.error_loading_album),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(CenterHorizontally)
                    )
                }
            }
        }
    }
}

@Composable
internal fun Screen(
    onNavigateUp: () -> Unit,
    onNavigateToPlayer: (trackIds: List<Long>, currentTrackId: Long) -> Unit,
    state: AlbumUiState.Success
) {
    val isPhoneScreen = !isLargeScreen()

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .consumeWindowInsets(WindowInsets.systemBars.only(WindowInsetsSides.Bottom)),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        item {
            AppHeader(
                title = stringResource(R.string.album),
                navigationIcon = { NavigationIcon(onNavigateUp = onNavigateUp) },
            )
        }
        item {
            val album = state.albumWithSongs.album
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                AlbumItemComponent(
                    title = album.title,
                    subtitle = album.artistName,
                    artworkUrl = album.coverUrl,
                    style = if (isPhoneScreen) MediaItemComponentStyle.COLUMN else MediaItemComponentStyle.ROW,
                    modifier = Modifier
                        .then(
                            if (isPhoneScreen) Modifier.align(Alignment.Center) else Modifier
                        ),
                )
            }
        }
        items(
            count = state.albumWithSongs.songs.size
        ) { index ->
            val song = state.albumWithSongs.songs[index]
            val trackId = song.trackId

            val isPreview = LocalInspectionMode.current
            val sharedTransitionScope = LocalSharedTransitionScope.current
            val animatedContentScope = if (!isPreview) LocalNavAnimatedContentScope.current else null
            val useSharedElements = trackId != null && !isPreview && sharedTransitionScope != null && animatedContentScope != null

            @OptIn(ExperimentalSharedTransitionApi::class)
            val artworkModifier = if (useSharedElements) {
                with(sharedTransitionScope!!) {
                    Modifier.sharedElement(
                        sharedContentState = rememberSharedContentState(key = "song_artwork_$trackId"),
                        animatedVisibilityScope = animatedContentScope!!,
                    )
                }
            } else Modifier

            @OptIn(ExperimentalSharedTransitionApi::class)
            val titleModifier = if (useSharedElements) {
                with(sharedTransitionScope!!) {
                    Modifier.sharedElement(
                        sharedContentState = rememberSharedContentState(key = "song_title_$trackId"),
                        animatedVisibilityScope = animatedContentScope!!,
                    )
                }
            } else Modifier

            @OptIn(ExperimentalSharedTransitionApi::class)
            val subtitleModifier = if (useSharedElements) {
                with(sharedTransitionScope!!) {
                    Modifier.sharedElement(
                        sharedContentState = rememberSharedContentState(key = "song_artist_$trackId"),
                        animatedVisibilityScope = animatedContentScope!!,
                    )
                }
            } else Modifier

            SongItemComponent(
                title = song.trackName,
                subtitle = song.artistName,
                artworkUrl = song.artworkUrl100,
                showNavOption = false,
                artworkModifier = artworkModifier,
                titleModifier = titleModifier,
                subtitleModifier = subtitleModifier,
                onClick = {
                    trackId?.let { safeTrackId ->
                        val allTrackIds = state.albumWithSongs.songs
                            .toMutableList()
                            .apply { remove(song) }
                            .mapNotNull { it.trackId }
                        onNavigateToPlayer(allTrackIds, safeTrackId)
                    }
                }
            )
        }
    }
}

@PhonePreviews
@TabletPreviews
@Composable
private fun Preview(
    @PreviewParameter(AlbumWithSongsPreviewParameterProvider::class) albumWithSongs: AlbumWithSongs
) {
    ThemePreview {
        Screen(
            onNavigateUp = {},
            onNavigateToPlayer = { _, _ -> },
            state = AlbumUiState.Success(albumWithSongs = albumWithSongs)
        )
    }
}
