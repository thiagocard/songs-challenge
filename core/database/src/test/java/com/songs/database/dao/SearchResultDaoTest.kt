package com.songs.database.dao

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isEmpty
import com.songs.database.BaseDaoTest
import com.songs.database.entity.SearchResultEntity
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SearchResultDaoTest : BaseDaoTest() {

    private fun entries(term: String, count: Int) =
        (0 until count).map { i -> SearchResultEntity(searchTerm = term, position = i, trackId = i.toLong()) }

    @Test
    fun `insertAll and countByTerm returns correct count`() = runTest {
        db.searchResultDao().insertAll(entries("pop", 3))

        assertThat(db.searchResultDao().countByTerm("pop")).isEqualTo(3)
    }

    @Test
    fun `countByTerm returns zero when no entries for term`() = runTest {
        assertThat(db.searchResultDao().countByTerm("jazz")).isEqualTo(0)
    }

    @Test
    fun `countByTerm is scoped to specific term`() = runTest {
        db.searchResultDao().insertAll(entries("pop", 5))
        db.searchResultDao().insertAll(entries("rock", 2))

        assertThat(db.searchResultDao().countByTerm("pop")).isEqualTo(5)
        assertThat(db.searchResultDao().countByTerm("rock")).isEqualTo(2)
    }

    @Test
    fun `insertAll replaces existing entry with same primary key`() = runTest {
        val original = SearchResultEntity(searchTerm = "pop", position = 0, trackId = 1L)
        val replaced = SearchResultEntity(searchTerm = "pop", position = 0, trackId = 99L)
        db.searchResultDao().insertAll(listOf(original))
        db.searchResultDao().insertAll(listOf(replaced))

        // Count should still be 1 (position 0 was replaced, not added)
        assertThat(db.searchResultDao().countByTerm("pop")).isEqualTo(1)
    }

    @Test
    fun `deleteByTerm removes only entries for that term`() = runTest {
        db.searchResultDao().insertAll(entries("pop", 4))
        db.searchResultDao().insertAll(entries("rock", 2))

        db.searchResultDao().deleteByTerm("pop")

        assertThat(db.searchResultDao().countByTerm("pop")).isEqualTo(0)
        assertThat(db.searchResultDao().countByTerm("rock")).isEqualTo(2)
    }

    @Test
    fun `deleteByTerm on non-existent term does not throw`() = runTest {
        db.searchResultDao().deleteByTerm("jazz")

        assertThat(db.searchResultDao().countByTerm("jazz")).isEqualTo(0)
    }
}
