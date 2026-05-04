package com.songs.home.mapper

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.songs.common.resource.ResourceProvider
import com.songs.home.data.model.ListSongsResponse
import com.songs.home.data.model.SongResponse
import com.songs.home.domain.mapper.SongsMapperImpl
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

class SongsMapperImplTest {

    private lateinit var mapper: SongsMapperImpl
    private val resourceProvider: ResourceProvider = mockk()

    @Before
    fun setUp() {
        every { resourceProvider.getString(any()) } returns "Unknown"
        mapper = SongsMapperImpl(resourceProvider)
    }

    @Test
    fun `map - maps all fields correctly from a full response`() {
        val response = ListSongsResponse(
            resultCount = 1,
            results = listOf(
                SongResponse(
                    wrapperType = "track",
                    kind = "song",
                    collectionId = 100L,
                    trackId = 1L,
                    artistName = "Artist",
                    collectionName = "Album",
                    trackName = "Track",
                    collectionCensoredName = "Album",
                    trackCensoredName = "Track",
                    collectionArtistId = 200L,
                    collectionArtistViewUrl = "https://artist.url",
                    collectionViewUrl = "https://collection.url",
                    trackViewUrl = "https://track.url",
                    previewUrl = "https://preview.url",
                    artworkUrl30 = "https://art30.url",
                    artworkUrl60 = "https://art60.url",
                    artworkUrl100 = "https://art100.url",
                    collectionPrice = 9.99,
                    trackPrice = 1.29,
                    trackRentalPrice = null,
                    collectionHdPrice = null,
                    trackHdPrice = null,
                    trackHdRentalPrice = null,
                    releaseDate = "2024-01-01T00:00:00Z",
                    collectionExplicitness = "notExplicit",
                    trackExplicitness = "notExplicit",
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
                )
            )
        )

        val songs = mapper.map(response)

        assertThat(songs).hasSize(1)
        val song = songs[0]
        assertThat(song.wrapperType).isEqualTo("track")
        assertThat(song.kind).isEqualTo("song")
        assertThat(song.collectionId).isEqualTo(100L)
        assertThat(song.trackId).isEqualTo(1L)
        assertThat(song.artistName).isEqualTo("Artist")
        assertThat(song.collectionName).isEqualTo("Album")
        assertThat(song.trackName).isEqualTo("Track")
        assertThat(song.previewUrl).isEqualTo("https://preview.url")
        assertThat(song.artworkUrl100).isEqualTo("https://art100.url")
        assertThat(song.trackPrice).isEqualTo(1.29)
        assertThat(song.country).isEqualTo("USA")
        assertThat(song.primaryGenreName).isEqualTo("Pop")
    }

    @Test
    fun `map - uses fallback string when artistName is null`() {
        val response = ListSongsResponse(
            resultCount = 1,
            results = listOf(SongResponse(artistName = null, trackName = "Track"))
        )

        val songs = mapper.map(response)

        assertThat(songs[0].artistName).isEqualTo("Unknown")
    }

    @Test
    fun `map - uses fallback string when trackName is null`() {
        val response = ListSongsResponse(
            resultCount = 1,
            results = listOf(SongResponse(artistName = "Artist", trackName = null))
        )

        val songs = mapper.map(response)

        assertThat(songs[0].trackName).isEqualTo("Unknown")
    }

    @Test
    fun `map - returns empty list when results is empty`() {
        val response = ListSongsResponse(resultCount = 0, results = emptyList())

        val songs = mapper.map(response)

        assertThat(songs).isEmpty()
    }

    @Test
    fun `map - maps multiple results correctly`() {
        val response = ListSongsResponse(
            resultCount = 3,
            results = listOf(
                SongResponse(artistName = "Artist 1", trackName = "Track 1", trackId = 1L),
                SongResponse(artistName = "Artist 2", trackName = "Track 2", trackId = 2L),
                SongResponse(artistName = "Artist 3", trackName = "Track 3", trackId = 3L),
            )
        )

        val songs = mapper.map(response)

        assertThat(songs).hasSize(3)
        assertThat(songs[0].trackName).isEqualTo("Track 1")
        assertThat(songs[1].trackName).isEqualTo("Track 2")
        assertThat(songs[2].trackName).isEqualTo("Track 3")
    }
}
