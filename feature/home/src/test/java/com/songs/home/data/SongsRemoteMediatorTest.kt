package com.songs.home.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import assertk.assertions.isFalse
import com.songs.database.SongsDatabase
import com.songs.database.dao.RemoteKeysDao
import com.songs.database.dao.SearchResultDao
import com.songs.database.dao.SongDao
import com.songs.database.entity.RemoteKeysEntity
import com.songs.database.entity.SongEntity
import com.songs.home.data.model.ListSongsResponse
import com.songs.home.data.model.SongResponse
import com.songs.home.data.remote.SongsRemoteDataSource
import com.songs.home.domain.mapper.SongsMapper
import com.songs.home.domain.model.Song
import com.songs.networking.NetworkResponse
import com.songs.support.mock.SongMock
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalPagingApi::class, ExperimentalCoroutinesApi::class)
class SongsRemoteMediatorTest {

    private val remoteDataSource: SongsRemoteDataSource = mockk()
    private val songsMapper: SongsMapper = mockk()
    private val database: SongsDatabase = mockk(relaxed = true)
    private val remoteKeysDao: RemoteKeysDao = mockk(relaxed = true)
    private val songDao: SongDao = mockk(relaxed = true)
    private val searchResultDao: SearchResultDao = mockk(relaxed = true)

    private val term = "rock"

    private fun buildMediator() = SongsRemoteMediator(term, remoteDataSource, songsMapper, database)

    private fun emptyPagingState() = PagingState<Int, SongEntity>(
        pages = emptyList(),
        anchorPosition = null,
        config = PagingConfig(pageSize = 20, initialLoadSize = 40),
        leadingPlaceholderCount = 0,
    )

    @Before
    fun setUp() {
        every { database.remoteKeysDao() } returns remoteKeysDao
        every { database.songDao() } returns songDao
        every { database.searchResultDao() } returns searchResultDao

        // Make withTransaction execute the lambda immediately
        mockkStatic("androidx.room.RoomDatabaseKt")
        val transactionLambdaSlot = slot<suspend () -> Unit>()
        coEvery { database.withTransaction(capture(transactionLambdaSlot)) } coAnswers {
            transactionLambdaSlot.captured.invoke()
        }

        coEvery { searchResultDao.countByTerm(any()) } returns 0
        coEvery { remoteKeysDao.getRemoteKeys(any()) } returns null
    }

    @Test
    fun `initialize returns LAUNCH_INITIAL_REFRESH when no cache exists`() = runTest {
        coEvery { remoteKeysDao.getRemoteKeys(term) } returns null
        val mediator = buildMediator()

        val result = mediator.initialize()

        assertThat(result).isEqualTo(RemoteMediator.InitializeAction.LAUNCH_INITIAL_REFRESH)
    }

    @Test
    fun `initialize returns SKIP_INITIAL_REFRESH when cache exists`() = runTest {
        coEvery { remoteKeysDao.getRemoteKeys(term) } returns RemoteKeysEntity(term, null, null)
        val mediator = buildMediator()

        val result = mediator.initialize()

        assertThat(result).isEqualTo(RemoteMediator.InitializeAction.SKIP_INITIAL_REFRESH)
    }

    @Test
    fun `PREPEND always returns endOfPaginationReached=true`() = runTest {
        val mediator = buildMediator()

        val result = mediator.load(LoadType.PREPEND, emptyPagingState())

        assertThat(result).isInstanceOf(RemoteMediator.MediatorResult.Success::class)
        assertThat((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached).isTrue()
    }

    @Test
    fun `REFRESH returns Error on network failure`() = runTest {
        coEvery { remoteDataSource.getSongs(term, any(), any()) } returns NetworkResponse.Error(Exception("Network error"))
        val mediator = buildMediator()

        val result = mediator.load(LoadType.REFRESH, emptyPagingState())

        assertThat(result).isInstanceOf(RemoteMediator.MediatorResult.Error::class)
    }

    @Test
    fun `REFRESH clears old data then inserts new songs`() = runTest {
        val songs = SongMock.songList
        coEvery { remoteDataSource.getSongs(term, any(), any()) } returns
            NetworkResponse.Success(ListSongsResponse(songs.size, songs.map { it.toSongResponse() }))
        coEvery { songsMapper.map(any()) } returns songs
        val mediator = buildMediator()

        mediator.load(LoadType.REFRESH, emptyPagingState())

        coVerify { remoteKeysDao.deleteByTerm(term) }
        coVerify { searchResultDao.deleteByTerm(term) }
        coVerify { songDao.deleteSongsByTerm(term) }
        coVerify { songDao.insertSongs(any()) }
    }

    @Test
    fun `REFRESH endOfPagination is true when results are fewer than limit`() = runTest {
        val songs = listOf(SongMock.song)
        coEvery { remoteDataSource.getSongs(term, any(), any()) } returns
            NetworkResponse.Success(ListSongsResponse(1, songs.map { it.toSongResponse() }))
        coEvery { songsMapper.map(any()) } returns songs
        val mediator = buildMediator()

        val result = mediator.load(LoadType.REFRESH, emptyPagingState())

        assertThat(result).isInstanceOf(RemoteMediator.MediatorResult.Success::class)
        assertThat((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached).isTrue()
    }

    @Test
    fun `REFRESH endOfPagination is false when results fill the page`() = runTest {
        val songs = (1..40).map { i -> SongMock.song.copy(trackId = i.toLong()) }
        coEvery { remoteDataSource.getSongs(term, any(), any()) } returns
            NetworkResponse.Success(ListSongsResponse(songs.size, songs.map { it.toSongResponse() }))
        coEvery { songsMapper.map(any()) } returns songs
        val mediator = buildMediator()

        val result = mediator.load(LoadType.REFRESH, emptyPagingState())

        assertThat(result).isInstanceOf(RemoteMediator.MediatorResult.Success::class)
        assertThat((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached).isFalse()
    }

    @Test
    fun `APPEND returns endOfPaginationReached=true when no remote keys exist`() = runTest {
        coEvery { remoteKeysDao.getRemoteKeys(term) } returns null
        val mediator = buildMediator()

        val result = mediator.load(LoadType.APPEND, emptyPagingState())

        assertThat(result).isInstanceOf(RemoteMediator.MediatorResult.Success::class)
        assertThat((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached).isTrue()
    }

    @Test
    fun `APPEND returns endOfPaginationReached=true when nextOffset is null`() = runTest {
        coEvery { remoteKeysDao.getRemoteKeys(term) } returns RemoteKeysEntity(term, nextOffset = null, prevOffset = null)
        val mediator = buildMediator()

        val result = mediator.load(LoadType.APPEND, emptyPagingState())

        assertThat(result).isInstanceOf(RemoteMediator.MediatorResult.Success::class)
        assertThat((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached).isTrue()
    }

    @Test
    fun `APPEND fetches next page using nextOffset`() = runTest {
        val nextOffset = 20
        coEvery { remoteKeysDao.getRemoteKeys(term) } returns RemoteKeysEntity(term, nextOffset = nextOffset, prevOffset = null)
        val songs = listOf(SongMock.song.copy(trackId = 99L))
        coEvery { remoteDataSource.getSongs(term, any(), nextOffset) } returns
            NetworkResponse.Success(ListSongsResponse(songs.size, songs.map { it.toSongResponse() }))
        coEvery { songsMapper.map(any()) } returns songs
        val mediator = buildMediator()

        val result = mediator.load(LoadType.APPEND, emptyPagingState())

        assertThat(result).isInstanceOf(RemoteMediator.MediatorResult.Success::class)
        coVerify { remoteDataSource.getSongs(term, any(), nextOffset) }
    }

    @Test
    fun `search_results positions are appended correctly on APPEND`() = runTest {
        val existingCount = 5
        coEvery { remoteKeysDao.getRemoteKeys(term) } returns RemoteKeysEntity(term, nextOffset = 20, prevOffset = null)
        coEvery { searchResultDao.countByTerm(term) } returns existingCount
        val songs = listOf(SongMock.song)
        coEvery { remoteDataSource.getSongs(term, any(), any()) } returns
            NetworkResponse.Success(ListSongsResponse(songs.size, songs.map { it.toSongResponse() }))
        coEvery { songsMapper.map(any()) } returns songs
        val mediator = buildMediator()

        mediator.load(LoadType.APPEND, emptyPagingState())

        coVerify {
            searchResultDao.insertAll(
                match { entities ->
                    entities.isNotEmpty() && entities[0].position == existingCount
                }
            )
        }
    }

    // Helper to convert Song → SongResponse for mock network responses
    private fun Song.toSongResponse() = SongResponse(
        wrapperType = wrapperType,
        kind = kind,
        collectionId = collectionId,
        trackId = trackId,
        artistName = artistName,
        collectionName = collectionName,
        trackName = trackName,
        artworkUrl100 = artworkUrl100,
    )
}
