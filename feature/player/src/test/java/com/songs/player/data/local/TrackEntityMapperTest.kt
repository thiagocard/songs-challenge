package com.songs.player.data.local

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.songs.database.entity.SongEntity
import com.songs.support.mock.TrackMock
import org.junit.Test

class TrackEntityMapperTest {

    private val track = TrackMock.track

    @Test
    fun toEntityMapsAllFields() {
        val entity = track.toEntity()
        assertThat(entity.trackId).isEqualTo(track.trackId ?: 0L)
        assertThat(entity.wrapperType).isEqualTo(track.wrapperType)
        assertThat(entity.kind).isEqualTo(track.kind)
        assertThat(entity.artistId).isEqualTo(track.artistId)
        assertThat(entity.collectionId).isEqualTo(track.collectionId)
        assertThat(entity.artistName).isEqualTo(track.artistName)
        assertThat(entity.collectionName).isEqualTo(track.collectionName)
        assertThat(entity.trackName).isEqualTo(track.trackName)
        assertThat(entity.previewUrl).isEqualTo(track.previewUrl)
        assertThat(entity.artworkUrl100).isEqualTo(track.artworkUrl100)
        assertThat(entity.artworkUrl500).isEqualTo(track.artworkUrl500)
        assertThat(entity.trackPrice).isEqualTo(track.trackPrice)
        assertThat(entity.releaseDate).isEqualTo(track.releaseDate)
        assertThat(entity.country).isEqualTo(track.country)
        assertThat(entity.primaryGenreName).isEqualTo(track.primaryGenreName)
        assertThat(entity.isStreamable).isEqualTo(track.isStreamable)
        assertThat(entity.discCount).isEqualTo(track.discCount)
        assertThat(entity.discNumber).isEqualTo(track.discNumber)
        assertThat(entity.trackCount).isEqualTo(track.trackCount)
        assertThat(entity.trackNumber).isEqualTo(track.trackNumber)
        assertThat(entity.trackTimeMillis).isEqualTo(track.trackTimeMillis)
    }

    @Test
    fun toEntityUses0LWhenTrackIdNull() {
        val entity = track.copy(trackId = null).toEntity()
        assertThat(entity.trackId).isEqualTo(0L)
    }

    @Test
    fun trackEntityToDomainRoundTrip() {
        val entity = track.toEntity()
        val domain = entity.toDomain()
        val entityAgain = domain.toEntity()
        assertThat(entityAgain).isEqualTo(entity)
    }

    @Test
    fun trackEntityToDomainPreservesArtistFields() {
        val entity = track.toEntity()
        val domain = entity.toDomain()
        assertThat(domain.artistId).isEqualTo(entity.artistId)
        assertThat(domain.artistViewUrl).isEqualTo(entity.artistViewUrl)
    }

    private fun buildSongEntity(
        artworkUrl100: String? = "https://example.com/100x100bb.jpg",
        trackId: Long = 10L,
    ) = SongEntity(
        trackId = trackId, searchTerm = "sample", albumId = 5L,
        wrapperType = "track", kind = "song", collectionId = 20L,
        artistName = "Artist", collectionName = "Album", trackName = "Song",
        collectionCensoredName = "Album", trackCensoredName = "Song",
        collectionArtistId = null, collectionArtistViewUrl = null,
        collectionViewUrl = null, trackViewUrl = null,
        previewUrl = "https://preview.url/song.m4a",
        artworkUrl30 = null, artworkUrl60 = null, artworkUrl100 = artworkUrl100,
        collectionPrice = 9.99, trackPrice = 1.29,
        trackRentalPrice = null, collectionHdPrice = null,
        trackHdPrice = null, trackHdRentalPrice = null,
        releaseDate = "2024-01-01T00:00:00Z",
        collectionExplicitness = "notExplicit", trackExplicitness = "notExplicit",
        trackCount = 12, trackNumber = 3, trackTimeMillis = 240000L,
        country = "USA", currency = "USD", primaryGenreName = "Pop",
        contentAdvisoryRating = null, shortDescription = null,
        longDescription = null, hasITunesExtras = false,
    )

    @Test
    fun songEntityArtworkUrl500DerivedFromUrl100() {
        val domain = buildSongEntity(artworkUrl100 = "https://example.com/100x100bb.jpg").toDomain()
        assertThat(domain.artworkUrl500).isEqualTo("https://example.com/500x500bb.jpg")
    }

    @Test
    fun songEntityArtworkUrl500NullWhenUrl100Null() {
        val domain = buildSongEntity(artworkUrl100 = null).toDomain()
        assertThat(domain.artworkUrl500).isNull()
    }

    @Test
    fun songEntityArtistIdAlwaysNull() {
        assertThat(buildSongEntity().toDomain().artistId).isNull()
    }

    @Test
    fun songEntityArtistViewUrlAlwaysNull() {
        assertThat(buildSongEntity().toDomain().artistViewUrl).isNull()
    }

    @Test
    fun songEntityMapsTrackIdAndCommonFields() {
        val entity = buildSongEntity(trackId = 42L)
        val domain = entity.toDomain()
        assertThat(domain.trackId).isEqualTo(42L)
        assertThat(domain.artistName).isEqualTo(entity.artistName)
        assertThat(domain.collectionName).isEqualTo(entity.collectionName)
        assertThat(domain.trackName).isEqualTo(entity.trackName)
        assertThat(domain.previewUrl).isEqualTo(entity.previewUrl)
        assertThat(domain.country).isEqualTo(entity.country)
        assertThat(domain.primaryGenreName).isEqualTo(entity.primaryGenreName)
    }
}
