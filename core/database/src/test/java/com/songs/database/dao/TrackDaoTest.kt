package com.songs.database.dao

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.songs.database.BaseDaoTest
import com.songs.database.entity.TrackEntity
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TrackDaoTest : BaseDaoTest() {

    private fun buildTrack(trackId: Long = 1L) = TrackEntity(
        trackId = trackId,
        wrapperType = "track",
        kind = "song",
        artistId = 10L,
        collectionId = 100L,
        artistName = "Artist $trackId",
        collectionName = "Album",
        trackName = "Track $trackId",
        collectionCensoredName = null,
        trackCensoredName = null,
        artistViewUrl = null,
        collectionArtistId = null,
        collectionArtistViewUrl = null,
        collectionViewUrl = null,
        trackViewUrl = null,
        previewUrl = null,
        artworkUrl30 = null,
        artworkUrl60 = null,
        artworkUrl100 = null,
        artworkUrl500 = null,
        collectionPrice = null,
        trackPrice = null,
        trackRentalPrice = null,
        collectionHdPrice = null,
        trackHdPrice = null,
        trackHdRentalPrice = null,
        releaseDate = null,
        collectionExplicitness = null,
        trackExplicitness = null,
        discCount = null,
        discNumber = null,
        trackCount = null,
        trackNumber = null,
        trackTimeMillis = null,
        country = null,
        currency = null,
        primaryGenreName = null,
        contentAdvisoryRating = null,
        shortDescription = null,
        longDescription = null,
        hasITunesExtras = null,
        isStreamable = null,
    )

    @Test
    fun `insertTracks and getTrackById returns inserted track`() = runTest {
        db.trackDao().insertTracks(listOf(buildTrack(1L)))

        val result = db.trackDao().getTrackById(1L)

        assertThat(result).hasSize(1)
        assertThat(result[0].trackId).isEqualTo(1L)
    }

    @Test
    fun `getTrackById returns empty list when track not found`() = runTest {
        assertThat(db.trackDao().getTrackById(99L)).isEmpty()
    }

    @Test
    fun `insertTracks replaces existing track with same trackId`() = runTest {
        val original = buildTrack(1L)
        val updated = original.copy(trackName = "Updated Track")
        db.trackDao().insertTracks(listOf(original))
        db.trackDao().insertTracks(listOf(updated))

        val result = db.trackDao().getTrackById(1L)

        assertThat(result).hasSize(1)
        assertThat(result[0].trackName).isEqualTo("Updated Track")
    }

    @Test
    fun `insertTracks stores multiple tracks`() = runTest {
        db.trackDao().insertTracks(listOf(buildTrack(1L), buildTrack(2L), buildTrack(3L)))

        assertThat(db.trackDao().getTrackById(1L)).hasSize(1)
        assertThat(db.trackDao().getTrackById(2L)).hasSize(1)
        assertThat(db.trackDao().getTrackById(3L)).hasSize(1)
    }

    @Test
    fun `getTrackById does not return a different track`() = runTest {
        db.trackDao().insertTracks(listOf(buildTrack(1L), buildTrack(2L)))

        val result = db.trackDao().getTrackById(1L)

        assertThat(result).hasSize(1)
        assertThat(result[0].trackId).isEqualTo(1L)
    }
}
