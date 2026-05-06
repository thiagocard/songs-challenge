package com.songs.core.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import assertk.assertThat
import assertk.assertions.isTrue
import com.songs.core.ui.theme.SongsTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class AppHeaderTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `displays title text`() {
        composeTestRule.setContent {
            SongsTheme {
                AppHeader(title = "Songs")
            }
        }

        composeTestRule.onNodeWithText("Songs").assertIsDisplayed()
    }

    @Test
    fun `shows navigation icon when provided`() {
        composeTestRule.setContent {
            SongsTheme {
                AppHeader(
                    title = "Now Playing",
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
    }

    @Test
    fun `does not show navigation icon when not provided`() {
        composeTestRule.setContent {
            SongsTheme {
                AppHeader(title = "Songs")
            }
        }

        composeTestRule.onNodeWithContentDescription("Back").assertDoesNotExist()
    }

    @Test
    fun `navigation icon click triggers callback`() {
        var clicked = false
        composeTestRule.setContent {
            SongsTheme {
                AppHeader(
                    title = "Now Playing",
                    navigationIcon = {
                        IconButton(onClick = { clicked = true }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Back").performClick()
        assertThat(clicked).isTrue()
    }

    @Test
    fun `shows trailing content when provided`() {
        composeTestRule.setContent {
            SongsTheme {
                AppHeader(
                    title = "Songs",
                    trailingContent = {
                        IconButton(onClick = {}) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "TrailingAction"
                            )
                        }
                    }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("TrailingAction").assertIsDisplayed()
    }
}
