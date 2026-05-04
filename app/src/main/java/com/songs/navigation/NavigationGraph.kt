package com.songs.navigation

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.songs.core.ui.transition.LocalSharedTransitionScope
import com.songs.home.navigation.albumScreen
import com.songs.home.navigation.songsScreen
import com.songs.navigation.route.AlbumRoute
import com.songs.navigation.route.HomeRoute
import com.songs.navigation.route.PlayerRoute
import com.songs.navigation.route.SplashRoute
import com.songs.player.miniplayer.MiniPlayer
import com.songs.player.miniplayer.MiniPlayerViewModel
import com.songs.player.navigation.playerScreen
import com.songs.splash.presentation.screens.SplashScreen

@Composable
fun NavigationGraph() {
    val backStack = rememberNavBackStack(SplashRoute)
    val miniPlayerViewModel: MiniPlayerViewModel = hiltViewModel()
    val isPlayerVisible = backStack.any { it is PlayerRoute }

    fun navigateToPlayer(trackIds: List<Long>, currentTrackId: Long) {
        val playerRoute = PlayerRoute(trackIds, currentTrackId, shouldPlay = true)
        val existingIndex = backStack.indexOfFirst { it is PlayerRoute }
        if (existingIndex >= 0) backStack.removeAt(existingIndex)
        backStack.add(playerRoute)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        // Tell Scaffold not to apply window insets itself — each screen manages its own top insets.
        contentWindowInsets = WindowInsets(),
        // MiniPlayer lives here so Compose automatically reserves its height as
        // bottom padding for all screen content — no screen can render behind it.
        bottomBar = {
            MiniPlayer(
                isPlayerScreenVisible = isPlayerVisible,
                onNavigateToPlayer = ::navigateToPlayer,
                viewModel = miniPlayerViewModel,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) { innerPadding ->
        SharedTransitionLayout {
            CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                NavDisplay(
                    backStack = backStack,
                    // Only apply the bottom padding (= MiniPlayer height) — top insets are handled
                    // by each screen's own Scaffold, so we must not double-apply them here.
                    modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
                    onBack = { backStack.removeLastOrNull() },
                    entryProvider = entryProvider {
                        entry<SplashRoute> {
                            SplashScreen(
                                onAnimationFinished = {
                                    backStack.removeLastOrNull() // remove SplashRoute
                                    backStack.add(HomeRoute)
                                }
                            )
                        }
                        songsScreen(
                            onNavigateToAlbum = { albumId -> backStack.add(AlbumRoute(albumId)) },
                            onNavigateToPlayer = ::navigateToPlayer,
                        )
                        albumScreen(
                            onNavigateUp = { backStack.removeLastOrNull() },
                            onNavigateToPlayer = ::navigateToPlayer
                        )
                        playerScreen(
                            onNavigateUp = { backStack.removeLastOrNull() },
                            onNavigateToAlbum = { albumId -> backStack.add(AlbumRoute(albumId)) },
                            onRouteChanged = miniPlayerViewModel::updatePlayerRoute,
                        )
                    }
                )
            } // CompositionLocalProvider
        } // SharedTransitionLayout
    }
}
