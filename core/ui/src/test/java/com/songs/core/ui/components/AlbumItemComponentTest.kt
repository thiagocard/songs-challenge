package com.songs.core.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.songs.core.ui.theme.SongsTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class AlbumItemComponentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `displays title and subtitle in ROW style`() {
        composeTestRule.setContent {
            SongsTheme {
                AlbumItemComponent(
                    title = "Thriller",
                    subtitle = "Michael Jackson",
                    artworkUrl = null,
                    style = MediaItemComponentStyle.ROW,
                )
            }
        }

        composeTestRule.onNodeWithText("Thriller").assertIsDisplayed()
        composeTestRule.onNodeWithText("Michael Jackson").assertIsDisplayed()
    }

    @Test
    fun `displays title and subtitle in COLUMN style`() {
        composeTestRule.setContent {
            SongsTheme {
                AlbumItemComponent(
                    title = "Abbey Road",
                    subtitle = "The Beatles",
                    artworkUrl = null,
                    style = MediaItemComponentStyle.COLUMN,
                )
            }
        }

        composeTestRule.onNodeWithText("Abbey Road").assertIsDisplayed()
        composeTestRule.onNodeWithText("The Beatles").assertIsDisplayed()
    }

    @Test
    fun `does not show nav option (AlbumItemComponent is not clickable)`() {
        composeTestRule.setContent {
            SongsTheme {
                AlbumItemComponent(
                    title = "Dark Side of the Moon",
                    subtitle = "Pink Floyd",
                    artworkUrl = null,
                    style = MediaItemComponentStyle.ROW,
                )
            }
        }

        // AlbumItemComponent always sets showNavOption = false
        composeTestRule.onNodeWithContentDescription("Menu").assertDoesNotExist()
    }
}
