package com.songs.core.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.songs.core.ui.theme.SongsTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class RowMediaItemComponentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val artworkSize = Dp(48f)
    private val cornerRadius = Dp(4f)

    @Test
    fun `displays title and subtitle`() {
        composeTestRule.setContent {
            SongsTheme {
                RowMediaItemComponent(
                    title = "Bohemian Rhapsody",
                    subtitle = "Queen",
                    artworkUrl = null,
                    artworkSize = artworkSize,
                    artworkCornerRadius = cornerRadius,
                    titleStyle = { MaterialTheme.typography.bodyLarge },
                    subtitleStyle = { MaterialTheme.typography.bodySmall },
                    subtitleAlpha = 1f,
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
                RowMediaItemComponent(
                    title = "Song",
                    subtitle = "Artist",
                    artworkUrl = null,
                    artworkSize = artworkSize,
                    artworkCornerRadius = cornerRadius,
                    titleStyle = { MaterialTheme.typography.bodyLarge },
                    subtitleStyle = { MaterialTheme.typography.bodySmall },
                    subtitleAlpha = 1f,
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
                RowMediaItemComponent(
                    title = "Song",
                    subtitle = "Artist",
                    artworkUrl = null,
                    artworkSize = artworkSize,
                    artworkCornerRadius = cornerRadius,
                    titleStyle = { MaterialTheme.typography.bodyLarge },
                    subtitleStyle = { MaterialTheme.typography.bodySmall },
                    subtitleAlpha = 1f,
                    isPlaying = false,
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Playing").assertDoesNotExist()
    }

    @Test
    fun `click triggers onClick callback when isClickable is true`() {
        var clicked = false
        composeTestRule.setContent {
            SongsTheme {
                RowMediaItemComponent(
                    title = "Clickable Song",
                    subtitle = "Artist",
                    artworkUrl = null,
                    artworkSize = artworkSize,
                    artworkCornerRadius = cornerRadius,
                    titleStyle = { MaterialTheme.typography.bodyLarge },
                    subtitleStyle = { MaterialTheme.typography.bodySmall },
                    subtitleAlpha = 1f,
                    isClickable = true,
                    onClick = { clicked = true },
                )
            }
        }

        composeTestRule.onNodeWithText("Clickable Song").performClick()
        assertThat(clicked).isTrue()
    }

    @Test
    fun `menu icon triggers onMenuItemClick with VIEW_ALBUM action`() {
        var menuAction: MediaDropdownMenuItemAction? = null
        composeTestRule.setContent {
            SongsTheme {
                RowMediaItemComponent(
                    title = "Song",
                    subtitle = "Artist",
                    artworkUrl = null,
                    artworkSize = artworkSize,
                    artworkCornerRadius = cornerRadius,
                    titleStyle = { MaterialTheme.typography.bodyLarge },
                    subtitleStyle = { MaterialTheme.typography.bodySmall },
                    subtitleAlpha = 1f,
                    showNavOption = true,
                    onMenuItemClick = { menuAction = it },
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Menu").performClick()
        composeTestRule.onNodeWithText("View album").performClick()
        assertThat(menuAction).isEqualTo(MediaDropdownMenuItemAction.VIEW_ALBUM)
    }
}
