package com.songs.home.songs

import androidx.paging.compose.LazyPagingItems
import com.songs.home.domain.model.Song
import io.mockk.every
import io.mockk.mockk

/**
 * A lightweight test double for [LazyPagingItems<Song>] backed by a plain [List].
 *
 * [LazyPagingItems] cannot be instantiated directly in unit tests because it requires
 * a Compose runtime. This helper creates a mock that delegates [itemCount], [peek], and
 * [itemSnapshotList.indexOfFirst] to the provided list — which is all [SongsViewModel.onSongClick]
 * needs.
 *
 * Since [ItemSnapshotList] is a final class, we mock [LazyPagingItems] using MockK's relaxed mode
 * and configure only the members that [SongsViewModel.onSongClick] actually accesses.
 */
fun FakeLazyPagingItems(items: List<Song>): LazyPagingItems<Song> {
    val mock = mockk<LazyPagingItems<Song>>(relaxed = true)

    // itemCount is used to iterate for peek()
    every { mock.itemCount } returns items.size

    // peek() is used to collect trackIds
    every { mock.peek(any()) } answers { items.getOrNull(firstArg()) }

    // itemSnapshotList.indexOfFirst is used to find the clicked song's index.
    // We mock the snapshot list as a list-like object using MockK.
    val snapshotMock = mockk<androidx.paging.ItemSnapshotList<Song>>(relaxed = true)
    every { snapshotMock.indexOfFirst(any()) } answers {
        val predicate = firstArg<(Song?) -> Boolean>()
        items.indexOfFirst { predicate(it) }
    }
    every { mock.itemSnapshotList } returns snapshotMock

    return mock
}
