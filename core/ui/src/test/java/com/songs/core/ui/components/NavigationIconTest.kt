package com.songs.core.ui.components

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
class NavigationIconTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `shows back arrow icon with Back content description`() {
        composeTestRule.setContent {
            SongsTheme {
                NavigationIcon(onNavigateUp = {})
            }
        }

        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
    }

    @Test
    fun `clicking the icon triggers onNavigateUp callback`() {
        var clicked = false
        composeTestRule.setContent {
            SongsTheme {
                NavigationIcon(onNavigateUp = { clicked = true })
            }
        }

        composeTestRule.onNodeWithContentDescription("Back").performClick()
        assertThat(clicked).isTrue()
    }
}
