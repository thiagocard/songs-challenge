package com.songs.home.presentation.ui.album
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.songs.core.ui.theme.SongsTheme
import com.songs.home.domain.model.AlbumInfo
import com.songs.home.domain.model.AlbumWithSongs
import com.songs.support.mock.SongMock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class AlbumScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val albumInfo = AlbumInfo(
        collectionId = 123L,
        title = "Thriller",
        artistName = "Michael Jackson",
        coverUrl = null,
    )

    private val songs = listOf(
        SongMock.song.copy(trackId = 1L, trackName = "Wanna Be Startin' Somethin'", artistName = "Michael Jackson"),
        SongMock.song.copy(trackId = 2L, trackName = "Beat It", artistName = "Michael Jackson"),
        SongMock.song.copy(trackId = 3L, trackName = "Billie Jean", artistName = "Michael Jackson"),
    )

    private val successState = AlbumUiState.Success(
        albumWithSongs = AlbumWithSongs(album = albumInfo, songs = songs)
    )

    @Test
    fun `shows LoadingScreen when state is Loading`() {
        composeTestRule.setContent {
            SongsTheme {
                AlbumScreen(
                    uiState = AlbumUiState.Loading,
                    onNavigateUp = {},
                    onNavigateToPlayer = { _, _ -> },
                )
            }
        }

        composeTestRule.onNode(
            hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)
        ).assertIsDisplayed()
    }

    @Test
    fun `shows error message when state is Error`() {
        composeTestRule.setContent {
            SongsTheme {
                AlbumScreen(
                    uiState = AlbumUiState.Error,
                    onNavigateUp = {},
                    onNavigateToPlayer = { _, _ -> },
                )
            }
        }

        composeTestRule.onNodeWithText("Error loading album").assertIsDisplayed()
    }

    @Test
    fun `shows album title and artist in Success state`() {
        composeTestRule.setContent {
            SongsTheme {
                AlbumScreen(
                    uiState = successState,
                    onNavigateUp = {},
                    onNavigateToPlayer = { _, _ -> },
                )
            }
        }

        composeTestRule.onNodeWithText("Thriller").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Michael Jackson")[0].assertIsDisplayed()
    }

    @Test
    fun `shows correct number of song items in Success state`() {
        composeTestRule.setContent {
            SongsTheme {
                AlbumScreen(
                    uiState = successState,
                    onNavigateUp = {},
                    onNavigateToPlayer = { _, _ -> },
                )
            }
        }

        composeTestRule.onNodeWithText("Wanna Be Startin' Somethin'").assertIsDisplayed()
        composeTestRule.onNodeWithText("Beat It").assertIsDisplayed()
        composeTestRule.onNodeWithText("Billie Jean").assertIsDisplayed()
    }

    @Test
    fun `clicking a song triggers onNavigateToPlayer with correct trackId`() {
        var navigatedTrackId: Long? = null
        composeTestRule.setContent {
            SongsTheme {
                AlbumScreen(
                    uiState = successState,
                    onNavigateUp = {},
                    onNavigateToPlayer = { _, currentTrackId -> navigatedTrackId = currentTrackId },
                )
            }
        }

        composeTestRule.onNodeWithText("Beat It").performClick()
        assertEquals(2L, navigatedTrackId)
    }

    @Test
    fun `tapping back calls onNavigateUp`() {
        var navigatedUp = false
        composeTestRule.setContent {
            SongsTheme {
                AlbumScreen(
                    uiState = successState,
                    onNavigateUp = { navigatedUp = true },
                    onNavigateToPlayer = { _, _ -> },
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Back").performClick()
        assertTrue(navigatedUp)
    }
}
