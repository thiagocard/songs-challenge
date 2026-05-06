package com.songs.navigation

import androidx.navigation3.runtime.NavKey
import com.songs.navigation.route.AlbumRoute
import com.songs.navigation.route.HomeRoute
import com.songs.navigation.route.PlayerRoute
import com.songs.navigation.route.SplashRoute
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationHandlerTest {

    @Test
    fun `isPlayerVisible - when splash is on top, then returns true`() {
        val backStack = mutableListOf<NavKey>(SplashRoute)
        val handler = NavigationHandler(backStack)

        assertTrue(handler.isPlayerVisible)
    }

    @Test
    fun `isPlayerVisible - when home is on top, then returns false`() {
        val backStack = mutableListOf<NavKey>(HomeRoute)
        val handler = NavigationHandler(backStack)

        assertFalse(handler.isPlayerVisible)
    }

    @Test
    fun `isPlayerVisible - when player is on top, then returns true`() {
        val backStack = mutableListOf(HomeRoute, PlayerRoute(emptyList(), 0L))
        val handler = NavigationHandler(backStack)

        assertTrue(handler.isPlayerVisible)
    }

    @Test
    fun `isPlayerVisible - when album is on top, then returns false`() {
        val backStack = mutableListOf(HomeRoute, PlayerRoute(emptyList(), 0L), AlbumRoute("1"))
        val handler = NavigationHandler(backStack)

        assertFalse(handler.isPlayerVisible)
    }

    @Test
    fun `navigateToPlayer - adds player route to backstack`() {
        val backStack = mutableListOf<NavKey>(HomeRoute)
        val handler = NavigationHandler(backStack)

        handler.navigateToPlayer(listOf(1L, 2L), 1L)

        assertEquals(2, backStack.size)
        assertTrue(backStack.last() is PlayerRoute)
        val playerRoute = backStack.last() as PlayerRoute
        assertEquals(listOf(1L, 2L), playerRoute.trackIds)
        assertEquals(1L, playerRoute.currentTrackId)
    }

    @Test
    fun `navigateToPlayer - removes existing player route before adding`() {
        val backStack = mutableListOf(HomeRoute, PlayerRoute(listOf(1L), 1L))
        val handler = NavigationHandler(backStack)

        handler.navigateToPlayer(listOf(2L), 2L)

        // It should have removed the old PlayerRoute and appended the new one
        assertEquals(2, backStack.size)
        assertEquals(HomeRoute, backStack[0])
        assertTrue(backStack[1] is PlayerRoute)
        val playerRoute = backStack[1] as PlayerRoute
        assertEquals(listOf(2L), playerRoute.trackIds)
        assertEquals(2L, playerRoute.currentTrackId)
    }

    @Test
    fun `navigateToAlbum - adds album route to backstack`() {
        val backStack = mutableListOf<NavKey>(HomeRoute)
        val handler = NavigationHandler(backStack)

        handler.navigateToAlbum("42")

        assertEquals(2, backStack.size)
        assertTrue(backStack.last() is AlbumRoute)
        val albumRoute = backStack.last() as AlbumRoute
        assertEquals("42", albumRoute.albumId)
    }

    @Test
    fun `navigateUp - removes last item from backstack`() {
        val backStack = mutableListOf(HomeRoute, AlbumRoute("1"))
        val handler = NavigationHandler(backStack)

        handler.navigateUp()

        assertEquals(1, backStack.size)
        assertEquals(HomeRoute, backStack.first())
    }

    @Test
    fun `removeSplashAndGoToHome - replaces splash with home`() {
        val backStack = mutableListOf<NavKey>(SplashRoute)
        val handler = NavigationHandler(backStack)

        handler.removeSplashAndGoToHome()

        assertEquals(1, backStack.size)
        assertEquals(HomeRoute, backStack.first())
    }
}
