package com.songs.player.presentation.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.songs.core.ui.theme.SongsTheme
import com.songs.player.domain.model.Track
import com.songs.support.mock.TrackMock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class PlayerScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val currentTrack = TrackMock.track.copy(
        trackId = 10L,
        collectionId = 777L,
        trackName = "Billie Jean",
        artistName = "Michael Jackson",
        trackTimeMillis = 240_000L,
    )

    @Test
    fun `shows loading indicator when player state is Loading`() {
        composeTestRule.setPlayerScreenContent(
            uiState = PlayerUiState.Loading,
            playlistUiState = PlaylistUiState.Loading,
        )

        composeTestRule.onNode(
            hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)
        ).assertIsDisplayed()
    }

    @Test
    fun `shows error message when player state is Error`() {
        composeTestRule.setPlayerScreenContent(
            uiState = PlayerUiState.Error,
            playlistUiState = PlaylistUiState.Loading,
        )

        composeTestRule.onNodeWithText("Error loading song").assertIsDisplayed()
    }

    @Test
    fun `shows now playing and track details in Success state`() {
        composeTestRule.setPlayerScreenContent(
            uiState = successState(currentTrack),
            playlistUiState = PlaylistUiState.Loading,
        )

        composeTestRule.onNodeWithText("Now playing").assertIsDisplayed()
        composeTestRule.onNodeWithText("Billie Jean").assertIsDisplayed()
        composeTestRule.onNodeWithText("Michael Jackson").assertIsDisplayed()
    }

    @Test
    fun `back button invokes onNavigateUp`() {
        var navigateUpCalls = 0

        composeTestRule.setPlayerScreenContent(
            uiState = successState(currentTrack),
            playlistUiState = PlaylistUiState.Loading,
            onNavigateUp = { navigateUpCalls++ },
        )

        composeTestRule.onNodeWithContentDescription("Back").performClick()

        assertEquals(1, navigateUpCalls)
    }

    @Test
    fun `menu action invokes onNavigateToAlbum with current collection id`() {
        var navigatedAlbumId: String? = null

        composeTestRule.setPlayerScreenContent(
            uiState = successState(currentTrack),
            playlistUiState = PlaylistUiState.Loading,
            onNavigateToAlbum = { navigatedAlbumId = it },
        )

        composeTestRule.onNodeWithContentDescription("Menu").performClick()
        composeTestRule.onNodeWithText("View album").performClick()

        assertEquals("777", navigatedAlbumId)
    }

    @Test
    fun `shows song progress labels in Success state`() {
        composeTestRule.setPlayerScreenContent(
            uiState = successState(currentTrack),
            playlistUiState = PlaylistUiState.Loading,
        )

        composeTestRule
            .onNodeWithText("01:00", useUnmergedTree = true)
            .fetchSemanticsNode()
        composeTestRule
            .onNodeWithText("-03:00", useUnmergedTree = true)
            .fetchSemanticsNode()
    }

    @Test
    fun `slider changes invoke onSliderPositionChange`() {
        val sliderPositions = mutableListOf<Long>()

        composeTestRule.setPlayerScreenContent(
            uiState = successState(currentTrack),
            playlistUiState = PlaylistUiState.Loading,
            onSliderPositionChange = { sliderPositions += it },
        )

        val setProgressMatcher = SemanticsMatcher.keyIsDefined(SemanticsActions.SetProgress)

        composeTestRule
            .onNode(setProgressMatcher)
            .performSemanticsAction(SemanticsActions.SetProgress) { setProgress ->
                setProgress(0.5f)
            }

        assertTrue(sliderPositions.contains(120_000L))
    }
}

private fun successState(track: Track) = PlayerUiState.Success(
    currentTrack = track,
    isPlaying = false,
    currentPosition = 60_000L,
    formattedCurrentPosition = "01:00",
    formattedRemainingTime = "-03:00",
    duration = 240_000L,
    sliderPosition = 0.25f,
    isRepeatOn = false,
)

private fun ComposeContentTestRule.setPlayerScreenContent(
    uiState: PlayerUiState,
    playlistUiState: PlaylistUiState,
    onNavigateUp: () -> Unit = {},
    onNavigateToAlbum: (String) -> Unit = {},
    onTrackClick: (Track) -> Unit = {},
    onPlayPauseClick: () -> Unit = {},
    onRewindClick: () -> Unit = {},
    onForwardClick: () -> Unit = {},
    onRepeatClick: () -> Unit = {},
    onSliderPositionChange: (Long) -> Unit = {},
) {
    setContent {
        CompositionLocalProvider(LocalInspectionMode provides true) {
            SongsTheme {
                PlayerScreen(
                    currentTrackId = 10L,
                    uiState = uiState,
                    playlistUiState = playlistUiState,
                    snackbarHostState = SnackbarHostState(),
                    onNavigateUp = onNavigateUp,
                    onNavigateToAlbum = onNavigateToAlbum,
                    onTrackClick = onTrackClick,
                    onPlayPauseClick = onPlayPauseClick,
                    onRewindClick = onRewindClick,
                    onForwardClick = onForwardClick,
                    onRepeatClick = onRepeatClick,
                    onSliderPositionChange = onSliderPositionChange,
                )
            }
        }
    }
}
