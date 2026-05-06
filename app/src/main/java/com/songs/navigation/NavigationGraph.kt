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
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.songs.core.ui.transition.LocalSharedTransitionScope
import com.songs.home.navigation.albumRoute
import com.songs.home.navigation.songsRoute
import com.songs.navigation.route.SplashRoute
import com.songs.player.miniplayer.MiniPlayer
import com.songs.player.miniplayer.MiniPlayerViewModel
import com.songs.player.navigation.playerRoute
import com.songs.splash.presentation.screens.SplashScreen

@Composable
fun NavigationGraph(
    startKey: NavKey = SplashRoute,
) {
    val backStack = rememberNavBackStack(startKey)
    val navigationHandler = rememberNavigationHandler(backStack)
    val miniPlayerViewModel: MiniPlayerViewModel = hiltViewModel()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        // Tell Scaffold not to apply window insets itself — each screen manages its own insets.
        contentWindowInsets = WindowInsets(),
        // MiniPlayer lives here so Compose automatically reserves its height as
        // bottom padding for all screen content — no screen can render behind it.
        bottomBar = {
            MiniPlayer(
                isPlayerScreenVisible = navigationHandler.isPlayerVisible,
                onNavigateToPlayer = navigationHandler::navigateToPlayer,
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
                    onBack = { navigationHandler.navigateUp() },
                    entryProvider = entryProvider {
                        entry<SplashRoute> {
                            SplashScreen(
                                onAnimationFinished = {
                                    navigationHandler.removeSplashAndGoToHome()
                                }
                            )
                        }
                        songsRoute(
                            onNavigateToAlbum = navigationHandler::navigateToAlbum,
                            onNavigateToPlayer = navigationHandler::navigateToPlayer,
                        )
                        albumRoute(
                            onNavigateUp = navigationHandler::navigateUp,
                            onNavigateToPlayer = navigationHandler::navigateToPlayer
                        )
                        playerRoute(
                            onNavigateUp = navigationHandler::navigateUp,
                            onNavigateToAlbum = navigationHandler::navigateToAlbum,
                            onNavigateToPlayer = navigationHandler::navigateToPlayer
                        )
                    }
                )
            }
        }
    }
}
