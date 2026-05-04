package com.songs.home.data.local

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.songs.database.entity.AlbumEntity
import com.songs.home.domain.model.AlbumInfo
import org.junit.Test

class AlbumEntityMapperTest {

    private val albumInfo = AlbumInfo(
        collectionId = 42L,
        title = "Test Album",
        artistName = "Test Artist",
        coverUrl = "https://example.com/cover.jpg",
    )

    @Test
    fun `toEntity - maps all fields correctly`() {
        val entity = albumInfo.toEntity()

        assertThat(entity.collectionId).isEqualTo(42L)
        assertThat(entity.title).isEqualTo("Test Album")
        assertThat(entity.artistName).isEqualTo("Test Artist")
        assertThat(entity.coverUrl).isEqualTo("https://example.com/cover.jpg")
    }

    @Test
    fun `toEntity - maps null coverUrl correctly`() {
        val entity = albumInfo.copy(coverUrl = null).toEntity()

        assertThat(entity.coverUrl).isNull()
    }

    @Test
    fun `toDomain - maps all fields correctly`() {
        val entity = AlbumEntity(
            collectionId = 42L,
            title = "Test Album",
            artistName = "Test Artist",
            coverUrl = "https://example.com/cover.jpg",
        )

        val domain = entity.toDomain()

        assertThat(domain.collectionId).isEqualTo(42L)
        assertThat(domain.title).isEqualTo("Test Album")
        assertThat(domain.artistName).isEqualTo("Test Artist")
        assertThat(domain.coverUrl).isEqualTo("https://example.com/cover.jpg")
    }

    @Test
    fun `round-trip toEntity then toDomain preserves all fields`() {
        val roundTrip = albumInfo.toEntity().toDomain()

        assertThat(roundTrip.collectionId).isEqualTo(albumInfo.collectionId)
        assertThat(roundTrip.title).isEqualTo(albumInfo.title)
        assertThat(roundTrip.artistName).isEqualTo(albumInfo.artistName)
        assertThat(roundTrip.coverUrl).isEqualTo(albumInfo.coverUrl)
    }

    @Test
    fun `round-trip with null coverUrl preserves null`() {
        val info = albumInfo.copy(coverUrl = null)
        val roundTrip = info.toEntity().toDomain()

        assertThat(roundTrip.coverUrl).isNull()
    }
}
