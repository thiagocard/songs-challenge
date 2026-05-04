package com.songs.home.data.local

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.songs.home.domain.model.AlbumInfo
import com.songs.support.mock.SongMock
import org.junit.Test

class SongEntityMapperTest {

    @Test
    fun `toEntity - maps all fields correctly`() {
        val song = SongMock.song
        val entity = song.toEntity("rock")

        assertThat(entity.searchTerm).isEqualTo("rock")
        assertThat(entity.trackId).isEqualTo(song.trackId!!)
        assertThat(entity.artistName).isEqualTo(song.artistName)
        assertThat(entity.trackName).isEqualTo(song.trackName)
        assertThat(entity.collectionId).isEqualTo(song.collectionId)
        assertThat(entity.albumId).isEqualTo(song.collectionId)
        assertThat(entity.previewUrl).isEqualTo(song.previewUrl)
        assertThat(entity.artworkUrl100).isEqualTo(song.artworkUrl100)
        assertThat(entity.trackPrice).isEqualTo(song.trackPrice)
        assertThat(entity.country).isEqualTo(song.country)
        assertThat(entity.primaryGenreName).isEqualTo(song.primaryGenreName)
        assertThat(entity.trackTimeMillis).isEqualTo(song.trackTimeMillis)
        assertThat(entity.releaseDate).isEqualTo(song.releaseDate)
    }

    @Test
    fun `toEntity - uses trackId 0 when trackId is null`() {
        val song = SongMock.song.copy(trackId = null)
        val entity = song.toEntity("pop")

        assertThat(entity.trackId).isEqualTo(0L)
    }

    @Test
    fun `toEntity - stores searchTerm correctly`() {
        val song = SongMock.song
        val entity = song.toEntity("my search term")

        assertThat(entity.searchTerm).isEqualTo("my search term")
    }

    @Test
    fun `toDomain - maps all fields back correctly`() {
        val entity = SongMock.song.toEntity("test")
        val domain = entity.toDomain()

        assertThat(domain.trackId).isEqualTo(entity.trackId)
        assertThat(domain.artistName).isEqualTo(entity.artistName)
        assertThat(domain.trackName).isEqualTo(entity.trackName)
        assertThat(domain.collectionId).isEqualTo(entity.collectionId)
        assertThat(domain.previewUrl).isEqualTo(entity.previewUrl)
        assertThat(domain.artworkUrl100).isEqualTo(entity.artworkUrl100)
        assertThat(domain.country).isEqualTo(entity.country)
        assertThat(domain.releaseDate).isEqualTo(entity.releaseDate)
    }

    @Test
    fun `round-trip toEntity then toDomain preserves all fields`() {
        val original = SongMock.song
        val roundTrip = original.toEntity("jazz").toDomain()

        assertThat(roundTrip.wrapperType).isEqualTo(original.wrapperType)
        assertThat(roundTrip.kind).isEqualTo(original.kind)
        assertThat(roundTrip.trackId).isEqualTo(original.trackId)
        assertThat(roundTrip.artistName).isEqualTo(original.artistName)
        assertThat(roundTrip.trackName).isEqualTo(original.trackName)
        assertThat(roundTrip.collectionId).isEqualTo(original.collectionId)
        assertThat(roundTrip.collectionName).isEqualTo(original.collectionName)
        assertThat(roundTrip.artworkUrl100).isEqualTo(original.artworkUrl100)
        assertThat(roundTrip.trackPrice).isEqualTo(original.trackPrice)
        assertThat(roundTrip.releaseDate).isEqualTo(original.releaseDate)
        assertThat(roundTrip.primaryGenreName).isEqualTo(original.primaryGenreName)
    }
}
