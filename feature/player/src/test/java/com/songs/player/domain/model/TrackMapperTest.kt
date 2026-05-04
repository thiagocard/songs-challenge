package com.songs.player.domain.model

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.songs.player.data.model.ListTrackResponse
import com.songs.player.data.model.TrackResponse
import org.junit.Test

class TrackMapperTest {

    private fun buildTrackResponse(
        artworkUrl100: String? = "https://example.com/100x100bb.jpg",
        trackId: Long? = 1L,
        trackName: String? = "Sample Song",
        artistName: String? = "Sample Artist",
    ) = TrackResponse(
        wrapperType = "track",
        kind = "song",
        artistId = 1L,
        collectionId = 10L,
        trackId = trackId,
        artistName = artistName,
        collectionName = "Sample Album",
        trackName = trackName,
        collectionCensoredName = "Sample Album",
        trackCensoredName = trackName,
        artistViewUrl = null,
        collectionArtistId = null,
        collectionArtistViewUrl = null,
        collectionViewUrl = null,
        trackViewUrl = null,
        previewUrl = "https://preview.url/song.m4a",
        artworkUrl30 = null,
        artworkUrl60 = null,
        artworkUrl100 = artworkUrl100,
        collectionPrice = 9.99,
        trackPrice = 1.29,
        trackRentalPrice = null,
        collectionHdPrice = null,
        trackHdPrice = null,
        trackHdRentalPrice = null,
        releaseDate = "2024-01-01T00:00:00Z",
        collectionExplicitness = "notExplicit",
        trackExplicitness = "notExplicit",
        discCount = 1,
        discNumber = 1,
        trackCount = 12,
        trackNumber = 3,
        trackTimeMillis = 240000L,
        country = "USA",
        currency = "USD",
        primaryGenreName = "Pop",
        contentAdvisoryRating = null,
        shortDescription = null,
        longDescription = null,
        hasITunesExtras = false,
        isStreamable = true,
    )

    @Test
    fun `toTrack - maps artworkUrl500 by replacing 100x100 with 500x500`() {
        val response = buildTrackResponse(artworkUrl100 = "https://example.com/100x100bb.jpg")

        val track = response.toTrack()

        assertThat(track.artworkUrl500).isEqualTo("https://example.com/500x500bb.jpg")
    }

    @Test
    fun `toTrack - artworkUrl500 is null when artworkUrl100 is null`() {
        val response = buildTrackResponse(artworkUrl100 = null)

        val track = response.toTrack()

        assertThat(track.artworkUrl500).isNull()
    }

    @Test
    fun `toTrack - preserves all other fields verbatim`() {
        val response = buildTrackResponse()

        val track = response.toTrack()

        assertThat(track.wrapperType).isEqualTo(response.wrapperType)
        assertThat(track.kind).isEqualTo(response.kind)
        assertThat(track.artistId).isEqualTo(response.artistId)
        assertThat(track.collectionId).isEqualTo(response.collectionId)
        assertThat(track.trackId).isEqualTo(response.trackId)
        assertThat(track.artistName).isEqualTo(response.artistName)
        assertThat(track.collectionName).isEqualTo(response.collectionName)
        assertThat(track.trackName).isEqualTo(response.trackName)
        assertThat(track.previewUrl).isEqualTo(response.previewUrl)
        assertThat(track.artworkUrl100).isEqualTo(response.artworkUrl100)
        assertThat(track.trackPrice).isEqualTo(response.trackPrice)
        assertThat(track.releaseDate).isEqualTo(response.releaseDate)
        assertThat(track.country).isEqualTo(response.country)
        assertThat(track.primaryGenreName).isEqualTo(response.primaryGenreName)
        assertThat(track.isStreamable).isEqualTo(response.isStreamable)
        assertThat(track.discCount).isEqualTo(response.discCount)
        assertThat(track.discNumber).isEqualTo(response.discNumber)
        assertThat(track.trackCount).isEqualTo(response.trackCount)
        assertThat(track.trackNumber).isEqualTo(response.trackNumber)
    }

    @Test
    fun `toListTracks - maps all results`() {
        val listResponse = ListTrackResponse(
            resultCount = 2,
            results = listOf(
                buildTrackResponse(trackId = 1L, trackName = "Track 1"),
                buildTrackResponse(trackId = 2L, trackName = "Track 2"),
            )
        )

        val tracks = listResponse.toListTracks()

        assertThat(tracks).hasSize(2)
        assertThat(tracks[0].trackName).isEqualTo("Track 1")
        assertThat(tracks[1].trackName).isEqualTo("Track 2")
    }

    @Test
    fun `toListTracks - returns empty list when results is empty`() {
        val listResponse = ListTrackResponse(resultCount = 0, results = emptyList())

        val tracks = listResponse.toListTracks()

        assertThat(tracks).hasSize(0)
    }
}
