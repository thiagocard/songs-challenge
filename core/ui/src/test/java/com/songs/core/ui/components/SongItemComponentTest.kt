package com.songs.core.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.songs.core.ui.theme.SongsTheme
import assertk.assertThat
import assertk.assertions.isTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class SongItemComponentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `displays title and subtitle`() {
        composeTestRule.setContent {
            SongsTheme {
                SongItemComponent(
                    title = "Bohemian Rhapsody",
                    subtitle = "Queen",
                    artworkUrl = null,
                )
            }
        }

        composeTestRule.onNodeWithText("Bohemian Rhapsody").assertIsDisplayed()
        composeTestRule.onNodeWithText("Queen").assertIsDisplayed()
    }

    @Test
    fun `shows playing indicator when isPlaying is true`() {
        composeTestRule.setContent {
            SongsTheme {
                SongItemComponent(
                    title = "Stairway to Heaven",
                    subtitle = "Led Zeppelin",
                    artworkUrl = null,
                    isPlaying = true,
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Playing").assertIsDisplayed()
    }

    @Test
    fun `does not show playing indicator when isPlaying is false`() {
        composeTestRule.setContent {
            SongsTheme {
                SongItemComponent(
                    title = "Stairway to Heaven",
                    subtitle = "Led Zeppelin",
                    artworkUrl = null,
                    isPlaying = false,
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Playing").assertDoesNotExist()
    }

    @Test
    fun `triggers onClick callback on click`() {
        var clicked = false
        composeTestRule.setContent {
            SongsTheme {
                SongItemComponent(
                    title = "Song Title",
                    subtitle = "Artist",
                    artworkUrl = null,
                    onClick = { clicked = true },
                )
            }
        }

        composeTestRule.onNodeWithText("Song Title").performClick()
        assertThat(clicked).isTrue()
    }

    @Test
    fun `hides nav option when showNavOption is false`() {
        composeTestRule.setContent {
            SongsTheme {
                SongItemComponent(
                    title = "Song Title",
                    subtitle = "Artist",
                    artworkUrl = null,
                    showNavOption = false,
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Menu").assertDoesNotExist()
    }

    @Test
    fun `shows nav option when showNavOption is true`() {
        composeTestRule.setContent {
            SongsTheme {
                SongItemComponent(
                    title = "Song Title",
                    subtitle = "Artist",
                    artworkUrl = null,
                    showNavOption = true,
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Menu").assertIsDisplayed()
    }
}
