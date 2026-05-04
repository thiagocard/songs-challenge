package com.songs.home.album
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.each
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.songs.home.TestCoroutineRule
import com.songs.home.domain.model.AlbumInfo
import com.songs.home.domain.model.AlbumWithSongs
import com.songs.home.domain.usecase.GetSongsByAlbumUseCase
import com.songs.home.presentation.ui.album.AlbumUiState
import com.songs.home.presentation.ui.album.AlbumViewModel
import com.songs.support.mock.SongMock
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AlbumViewModelTest {

    private lateinit var albumViewModel: AlbumViewModel
    private lateinit var getSongsByAlbumUseCase: GetSongsByAlbumUseCase

    @get:Rule
    var testCoroutineRule = TestCoroutineRule()

    private val mockAlbumInfo = AlbumInfo(
        collectionId = 123L,
        title = "Album 1",
        coverUrl = "http://art100.com",
        artistName = "Artist 1",
    )
    private val mockTracks = listOf(
        SongMock.song.copy(
            trackId = 1L, trackName = "Track 1", trackCensoredName = "Track 1", trackNumber = 1,
            artistName = "Artist 1", collectionName = "Album 1", collectionCensoredName = "Album 1",
            collectionId = 123L,
        ),
        SongMock.song.copy(
            trackId = 2L, trackName = "Track 2", trackCensoredName = "Track 2", trackNumber = 2,
            artistName = "Artist 1", collectionName = "Album 1", collectionCensoredName = "Album 1",
            collectionId = 123L,
        ),
    )
    private val mockAlbumWithSongs = AlbumWithSongs(album = mockAlbumInfo, songs = mockTracks)
    @Before
    fun setUp() {
        getSongsByAlbumUseCase = mockk()
        coEvery { getSongsByAlbumUseCase("123") } returns flowOf(mockAlbumWithSongs)
        albumViewModel = AlbumViewModel(
            getSongsByAlbumUseCase = getSongsByAlbumUseCase,
            albumId = "123"
        )
    }
    @Test
    fun `given a valid albumId, when the ViewModel is initialized, then uiState emits Success with the album tracks`() {
        runTest(testCoroutineRule.dispatcher) {
            advanceUntilIdle()
            albumViewModel.uiState.test {
                val state = awaitItem()
                assertThat(state).isInstanceOf(AlbumUiState.Success::class)
                val success = state as AlbumUiState.Success
                assertThat(success.albumWithSongs.songs).hasSize(2)
                assertThat(success.albumWithSongs.songs[0].trackName).isEqualTo("Track 1")
                assertThat(success.albumWithSongs.songs[1].trackName).isEqualTo("Track 2")
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
    @Test
    fun `given a valid albumId, when songs are loaded, then uiState emits Success with correct track IDs`() {
        runTest(testCoroutineRule.dispatcher) {
            advanceUntilIdle()
            albumViewModel.uiState.test {
                val state = awaitItem() as AlbumUiState.Success
                assertThat(state.albumWithSongs.songs[0].trackId).isEqualTo(1L)
                assertThat(state.albumWithSongs.songs[1].trackId).isEqualTo(2L)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
    @Test
    fun `given a valid albumId, when GetSongsByAlbumUseCase throws an exception, then uiState emits Error`() {
        runTest(testCoroutineRule.dispatcher) {
            coEvery { getSongsByAlbumUseCase("123") } throws Exception("Network error")
            val viewModel = AlbumViewModel(getSongsByAlbumUseCase, albumId = "123")
            advanceUntilIdle()
            viewModel.uiState.test {
                assertThat(awaitItem()).isInstanceOf(AlbumUiState.Error::class)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
    @Test
    fun `given an albumId with no songs, when songs are loaded, then uiState emits Error`() {
        runTest(testCoroutineRule.dispatcher) {
            val emptyAlbumWithSongs = AlbumWithSongs(album = mockAlbumInfo, songs = emptyList())
            coEvery { getSongsByAlbumUseCase("456") } returns flowOf(emptyAlbumWithSongs)
            val viewModel = AlbumViewModel(getSongsByAlbumUseCase, albumId = "456")
            advanceUntilIdle()
            viewModel.uiState.test {
                assertThat(awaitItem()).isInstanceOf(AlbumUiState.Error::class)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
    @Test
    fun `given a valid albumId, when the ViewModel is initialized, then GetSongsByAlbumUseCase is called with the correct albumId`() {
        runTest(testCoroutineRule.dispatcher) {
            advanceUntilIdle()
            coVerify { getSongsByAlbumUseCase("123") }
        }
    }
    @Test
    fun `given an empty albumId, when the ViewModel is initialized, then GetSongsByAlbumUseCase is called with an empty string`() {
        runTest(testCoroutineRule.dispatcher) {
            coEvery { getSongsByAlbumUseCase("") } returns flowOf(mockAlbumWithSongs)
            AlbumViewModel(getSongsByAlbumUseCase, albumId = "")
            advanceUntilIdle()
            coVerify { getSongsByAlbumUseCase("") }
        }
    }
    @Test
    fun `given a valid albumId, when songs are loaded, then all items in Success state are tracks`() {
        runTest(testCoroutineRule.dispatcher) {
            advanceUntilIdle()
            albumViewModel.uiState.test {
                val state = awaitItem() as AlbumUiState.Success
                assertThat(state.albumWithSongs.songs).each { song ->
                    song.transform { it.wrapperType }.isEqualTo("track")
                    song.transform { it.kind }.isEqualTo("song")
                }
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
    @Test
    fun `given an albumId with many tracks, when songs are loaded, then uiState emits Success with all tracks`() {
        runTest(testCoroutineRule.dispatcher) {
            val largeSongs = mockTracks + mockTracks + mockTracks + mockTracks
            val largeAlbumWithSongs = AlbumWithSongs(album = mockAlbumInfo, songs = largeSongs)
            coEvery { getSongsByAlbumUseCase("999") } returns flowOf(largeAlbumWithSongs)
            val viewModel = AlbumViewModel(getSongsByAlbumUseCase, albumId = "999")
            advanceUntilIdle()
            viewModel.uiState.test {
                val state = awaitItem() as AlbumUiState.Success
                assertThat(state.albumWithSongs.songs).hasSize(8)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
    @Test
    fun `given a valid albumId, when songs are loaded, then track properties are preserved correctly`() {
        runTest(testCoroutineRule.dispatcher) {
            advanceUntilIdle()
            albumViewModel.uiState.test {
                val state = awaitItem() as AlbumUiState.Success
                val firstTrack = state.albumWithSongs.songs[0]
                assertThat(firstTrack.trackName).isEqualTo("Track 1")
                assertThat(firstTrack.artistName).isEqualTo("Artist 1")
                assertThat(firstTrack.collectionName).isEqualTo("Album 1")
                assertThat(firstTrack.trackId).isEqualTo(1L)
                assertThat(firstTrack.collectionId).isEqualTo(123L)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
}
