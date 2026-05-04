package com.songs.database.dao

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.songs.database.BaseDaoTest
import com.songs.database.entity.AlbumEntity
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AlbumDaoTest : BaseDaoTest() {

    private val albumEntity = AlbumEntity(
        collectionId = 123L,
        title = "Sample Album",
        artistName = "Sample Artist",
        coverUrl = "https://example.com/artwork.jpg",
    )

    @Test
    fun `insertAlbum and getAlbumById returns inserted album`() = runTest {
        db.albumDao().insertAlbum(albumEntity)

        val result = db.albumDao().getAlbumById("123")

        assertThat(result).isEqualTo(albumEntity)
    }

    @Test
    fun `getAlbumById returns null when album does not exist`() = runTest {
        val result = db.albumDao().getAlbumById("999")

        assertThat(result).isNull()
    }

    @Test
    fun `insertAlbum replaces existing album with same collectionId`() = runTest {
        val updated = albumEntity.copy(title = "Updated Title")
        db.albumDao().insertAlbum(albumEntity)
        db.albumDao().insertAlbum(updated)

        val result = db.albumDao().getAlbumById("123")

        assertThat(result?.title).isEqualTo("Updated Title")
    }

    @Test
    fun `getAlbumById returns correct album when multiple albums exist`() = runTest {
        val second = AlbumEntity(
            collectionId = 456L,
            title = "Second Album",
            artistName = "Other Artist",
            coverUrl = null,
        )
        db.albumDao().insertAlbum(albumEntity)
        db.albumDao().insertAlbum(second)

        assertThat(db.albumDao().getAlbumById("123")).isEqualTo(albumEntity)
        assertThat(db.albumDao().getAlbumById("456")).isEqualTo(second)
    }
}
