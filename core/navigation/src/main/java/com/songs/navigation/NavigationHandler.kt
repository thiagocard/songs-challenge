package com.songs.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavKey
import com.songs.navigation.route.AlbumRoute
import com.songs.navigation.route.HomeRoute
import com.songs.navigation.route.PlayerRoute
import com.songs.navigation.route.SplashRoute

/**
 * NavigationHandler manages the navigation back stack and provides helper functions to navigate
 * between screens.
 */
class NavigationHandler(
    val backStack: MutableList<NavKey>
) {
    val isPlayerVisible: Boolean
        get() = backStack.lastOrNull() is PlayerRoute || backStack.lastOrNull() is SplashRoute

    fun navigateToPlayer(trackIds: List<Long>, currentTrackId: Long, shouldPlay: Boolean = true) {
        val playerRoute = PlayerRoute(trackIds, currentTrackId, shouldPlay)
        val existingIndex = backStack.indexOfFirst { it is PlayerRoute }
        if (existingIndex >= 0) {
            backStack.removeAt(existingIndex)
        }
        backStack.add(playerRoute)
    }

    fun navigateToAlbum(albumId: String) {
        backStack.add(AlbumRoute(albumId))
    }

    fun navigateUp() {
        backStack.removeLastOrNull()
    }

    fun removeSplashAndGoToHome() {
        backStack.removeLastOrNull() // remove SplashRoute
        backStack.add(HomeRoute)
    }
}

@Composable
fun rememberNavigationHandler(backStack: MutableList<NavKey>): NavigationHandler {
    return remember(backStack) {
        NavigationHandler(backStack)
    }
}
