package com.songs.database.dao

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.songs.database.BaseDaoTest
import com.songs.database.entity.RemoteKeysEntity
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RemoteKeysDaoTest : BaseDaoTest() {

    @Test
    fun `insertOrReplace and getRemoteKeys returns inserted keys`() = runTest {
        val entity = RemoteKeysEntity(searchTerm = "pop", nextOffset = 10, prevOffset = null)
        db.remoteKeysDao().insertOrReplace(entity)

        assertThat(db.remoteKeysDao().getRemoteKeys("pop")).isEqualTo(entity)
    }

    @Test
    fun `getRemoteKeys returns null when no keys for term`() = runTest {
        assertThat(db.remoteKeysDao().getRemoteKeys("jazz")).isNull()
    }

    @Test
    fun `insertOrReplace overwrites existing keys for same term`() = runTest {
        db.remoteKeysDao().insertOrReplace(RemoteKeysEntity("pop", nextOffset = 10, prevOffset = null))
        db.remoteKeysDao().insertOrReplace(RemoteKeysEntity("pop", nextOffset = 20, prevOffset = 10))

        val result = db.remoteKeysDao().getRemoteKeys("pop")

        assertThat(result?.nextOffset).isEqualTo(20)
        assertThat(result?.prevOffset).isEqualTo(10)
    }

    @Test
    fun `deleteByTerm removes keys for that term only`() = runTest {
        db.remoteKeysDao().insertOrReplace(RemoteKeysEntity("pop", 10, null))
        db.remoteKeysDao().insertOrReplace(RemoteKeysEntity("rock", 5, null))

        db.remoteKeysDao().deleteByTerm("pop")

        assertThat(db.remoteKeysDao().getRemoteKeys("pop")).isNull()
        assertThat(db.remoteKeysDao().getRemoteKeys("rock")?.nextOffset).isEqualTo(5)
    }

    @Test
    fun `deleteByTerm on non-existent term does not throw`() = runTest {
        db.remoteKeysDao().deleteByTerm("jazz")

        assertThat(db.remoteKeysDao().getRemoteKeys("jazz")).isNull()
    }

    @Test
    fun `keys with null nextOffset are persisted correctly`() = runTest {
        val entity = RemoteKeysEntity(searchTerm = "pop", nextOffset = null, prevOffset = null)
        db.remoteKeysDao().insertOrReplace(entity)

        assertThat(db.remoteKeysDao().getRemoteKeys("pop")?.nextOffset).isNull()
    }
}
