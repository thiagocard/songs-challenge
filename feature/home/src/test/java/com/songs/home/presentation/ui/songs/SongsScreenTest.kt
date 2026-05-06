package com.songs.home.presentation.ui.songs

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.songs.core.ui.theme.SongsTheme
import com.songs.home.domain.model.Song
import com.songs.support.mock.SongMock
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class SongsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val songs = listOf(
        SongMock.song.copy(
            collectionId = 101L,
            trackId = 1L,
            trackName = "First Song",
            artistName = "First Artist",
        ),
        SongMock.song.copy(
            collectionId = 202L,
            trackId = 2L,
            trackName = "Second Song",
            artistName = "Second Artist",
        ),
        SongMock.song.copy(
            collectionId = 303L,
            trackId = 3L,
            trackName = "Third Song",
            artistName = "Third Artist",
        ),
    )

    @Test
    fun `shows loading indicator during initial refresh`() {
        composeTestRule.setSongsScreenContent(
            pagingDataFlow = pagerFlow(items = songs, delayMillis = 5_000),
        )

        composeTestRule.waitUntilNodeWithProgressIndicatorExists()

        composeTestRule.onNode(
            hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)
        ).assertIsDisplayed()
    }

    @Test
    fun `shows error message when refresh fails with no items`() {
        composeTestRule.setSongsScreenContent(
            pagingDataFlow = errorPagingFlow(error = IllegalStateException("boom")),
        )
        composeTestRule.waitUntilNodeWithTextExists(
            text = "Error loading songs",
        )

        composeTestRule.onNodeWithText("Error loading songs", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun `shows songs header and list items when paging data succeeds`() {
        composeTestRule.setSongsScreenContent(
            pagingDataFlow = successPagingFlow(items = songs),
        )
        composeTestRule.awaitPagingIdle()
        composeTestRule.waitUntilNodeWithTextExists("First Song")

        composeTestRule.onNodeWithText("Songs").assertIsDisplayed()
        composeTestRule.onNodeWithText("First Song", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Second Song", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Third Song", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun `song click forwards selected song`() {
        var clickedSong: Song? = null

        composeTestRule.setSongsScreenContent(
            pagingDataFlow = successPagingFlow(items = songs),
            onSongClick = { clickedSong = it },
        )
        composeTestRule.awaitPagingIdle()
        composeTestRule.waitUntilNodeWithTextExists("Second Song")

        composeTestRule.onNodeWithText("Second Song", useUnmergedTree = true).performClick()

        assertEquals(2L, clickedSong?.trackId)
    }

    @Test
    fun `menu action navigates to album`() {
        var navigatedAlbumId: String? = null

        composeTestRule.setSongsScreenContent(
            pagingDataFlow = successPagingFlow(items = songs),
            onNavigateToAlbum = { navigatedAlbumId = it },
        )
        composeTestRule.awaitPagingIdle()
        composeTestRule.waitUntilNodeWithContentDescriptionExists(
            contentDescription = "Menu",
        )

        composeTestRule.onAllNodesWithContentDescription("Menu", useUnmergedTree = true)[0].performClick()
        composeTestRule.onNodeWithText("View album").performClick()

        assertEquals("101", navigatedAlbumId)
    }

    @Test
    fun `activating search and typing forwards search term changes`() {
        val searchTerms = mutableListOf<String>()

        composeTestRule.setSongsScreenContent(
            pagingDataFlow = successPagingFlow(items = songs),
            onSearchTermChanged = { searchTerms += it },
        )
        composeTestRule.awaitPagingIdle()
        composeTestRule.waitUntilNodeWithTextExists("Songs")

        composeTestRule.onNodeWithText("Songs").assertIsDisplayed()
        composeTestRule.onAllNodesWithContentDescription("Search your library")[0].performClick()
        composeTestRule.onNode(hasSetTextAction()).performTextInput("rock")

        // Close search mode so focus/keyboard side effects do not leak across tests on Robolectric.
        composeTestRule.onAllNodesWithContentDescription("Search your library")[0].performClick()
        composeTestRule.awaitPagingIdle()

        assertTrue(searchTerms.contains("rock"))
    }

    @Test
    fun `shows append loading indicator when more pages are loading`() {
        composeTestRule.setSongsScreenContent(
            pagingDataFlow = flowOf(
                PagingData.from(
                    data = songs,
                    sourceLoadStates = LoadStates(
                        refresh = LoadState.NotLoading(endOfPaginationReached = false),
                        prepend = LoadState.NotLoading(endOfPaginationReached = true),
                        append = LoadState.Loading,
                    ),
                )
            ),
        )
        composeTestRule.awaitPagingIdle()
        composeTestRule.waitUntilNodeWithTextExists("First Song")

        composeTestRule.onNode(
            hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)
        ).assertIsDisplayed()
    }

    @Test
    fun `empty list shows header but no song items`() {
        composeTestRule.setSongsScreenContent(
            pagingDataFlow = successPagingFlow(items = emptyList()),
        )
        composeTestRule.awaitPagingIdle()
        composeTestRule.waitUntilNodeWithTextExists("Songs")

        composeTestRule.onNodeWithText("Songs").assertIsDisplayed()
        assertTrue(
            composeTestRule.onAllNodesWithText("First Song", useUnmergedTree = true)
                .fetchSemanticsNodes().isEmpty()
        )
    }

    @Test
    fun `song with null collectionId does not trigger album navigation`() {
        var navigatedAlbumId: String? = null

        val songsWithNullAlbum = listOf(
            SongMock.song.copy(
                collectionId = null,
                trackId = 1L,
                trackName = "No Album Song",
                artistName = "Artist",
            )
        )

        composeTestRule.setSongsScreenContent(
            pagingDataFlow = successPagingFlow(items = songsWithNullAlbum),
            onNavigateToAlbum = { navigatedAlbumId = it },
        )
        composeTestRule.awaitPagingIdle()
        composeTestRule.waitUntilNodeWithContentDescriptionExists("Menu")

        composeTestRule.onAllNodesWithContentDescription("Menu", useUnmergedTree = true)[0].performClick()
        composeTestRule.onNodeWithText("View album").performClick()

        assertNull(navigatedAlbumId)
    }

    @Test
    fun `clicking first song forwards first song`() {
        var clickedSong: Song? = null

        composeTestRule.setSongsScreenContent(
            pagingDataFlow = successPagingFlow(items = songs),
            onSongClick = { clickedSong = it },
        )
        composeTestRule.awaitPagingIdle()
        composeTestRule.waitUntilNodeWithTextExists("First Song")

        composeTestRule.onNodeWithText("First Song", useUnmergedTree = true).performClick()

        assertEquals(1L, clickedSong?.trackId)
    }
}


private fun ComposeContentTestRule.setSongsScreenContent(
    pagingDataFlow: Flow<PagingData<Song>>,
    searchTerm: String = "",
    onSearchTermChanged: (String) -> Unit = {},
    onResetToDefault: () -> Unit = {},
    onNavigateToAlbum: (String) -> Unit = {},
    onSongClick: (Song) -> Unit = {},
) {
    setContent {
        CompositionLocalProvider(LocalInspectionMode provides true) {
            SongsTheme {
                val pagingItems = pagingDataFlow.collectAsLazyPagingItems()
                SongsScreen(
                    searchTerm = searchTerm,
                    pagingItems = pagingItems,
                    onSearchTermChanged = onSearchTermChanged,
                    onNavigateToAlbum = onNavigateToAlbum,
                    onSongClick = onSongClick,
                )
            }
        }
    }
}

private fun ComposeContentTestRule.awaitPagingIdle(
) {
    waitForIdle()
}

private fun ComposeContentTestRule.waitUntilNodeWithProgressIndicatorExists() {
    waitUntil(timeoutMillis = 5_000) {
        onAllNodes(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .fetchSemanticsNodes().isNotEmpty()
    }
}

private fun ComposeContentTestRule.waitUntilNodeWithTextExists(
    text: String,
) {
    try {
        waitUntil(timeoutMillis = 5_000) {
            onAllNodesWithText(text, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
        }
    } catch (error: Throwable) {
        val rootNode = onRoot(useUnmergedTree = true).fetchSemanticsNode().toString()
        throw AssertionError("Node with text '$text' was not found. Root node: $rootNode", error)
    }
}

private fun ComposeContentTestRule.waitUntilNodeWithContentDescriptionExists(
    contentDescription: String,
) {
    waitUntil(timeoutMillis = 5_000) {
        onAllNodesWithContentDescription(contentDescription, useUnmergedTree = true)
            .fetchSemanticsNodes().isNotEmpty()
    }
}

private fun pagerFlow(
    items: List<Song> = emptyList(),
    error: Throwable? = null,
    delayMillis: Long = 0L,
): Flow<PagingData<Song>> = Pager(PagingConfig(pageSize = 20)) {
    TestSongPagingSource(items, error, delayMillis)
}.flow

private fun successPagingFlow(items: List<Song>): Flow<PagingData<Song>> =
    flowOf(
        PagingData.from(
            data = items,
            sourceLoadStates = LoadStates(
                refresh = LoadState.NotLoading(endOfPaginationReached = false),
                prepend = LoadState.NotLoading(endOfPaginationReached = true),
                append = LoadState.NotLoading(endOfPaginationReached = true),
            ),
        )
    )

private fun errorPagingFlow(error: Throwable): Flow<PagingData<Song>> =
    flowOf(
        PagingData.empty(
            sourceLoadStates = LoadStates(
                refresh = LoadState.Error(error),
                prepend = LoadState.NotLoading(endOfPaginationReached = true),
                append = LoadState.NotLoading(endOfPaginationReached = true),
            ),
        )
    )


private class TestSongPagingSource(
    private val items: List<Song>,
    private val error: Throwable?,
    private val delayMillis: Long,
) : PagingSource<Int, Song>() {
    override fun getRefreshKey(state: PagingState<Int, Song>): Int? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Song> {
        if (delayMillis > 0) delay(delayMillis)
        error?.let { return LoadResult.Error(it) }
        return LoadResult.Page(
            data = items,
            prevKey = null,
            nextKey = null,
        )
    }
}
