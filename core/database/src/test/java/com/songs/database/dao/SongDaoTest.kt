package com.songs.database.dao

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.songs.database.BaseDaoTest
import com.songs.database.entity.SongEntity
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SongDaoTest : BaseDaoTest() {

    private fun buildSong(
        trackId: Long = 1L,
        searchTerm: String = "pop",
        albumId: Long? = 100L,
        wrapperType: String? = "track",
    ) = SongEntity(
        trackId = trackId,
        searchTerm = searchTerm,
        albumId = albumId,
        wrapperType = wrapperType,
        kind = "song",
        collectionId = albumId,
        artistName = "Artist $trackId",
        collectionName = "Album",
        trackName = "Track $trackId",
        collectionCensoredName = null,
        trackCensoredName = null,
        collectionArtistId = null,
        collectionArtistViewUrl = null,
        collectionViewUrl = null,
        trackViewUrl = null,
        previewUrl = null,
        artworkUrl30 = null,
        artworkUrl60 = null,
        artworkUrl100 = null,
        collectionPrice = null,
        trackPrice = null,
        trackRentalPrice = null,
        collectionHdPrice = null,
        trackHdPrice = null,
        trackHdRentalPrice = null,
        releaseDate = null,
        collectionExplicitness = null,
        trackExplicitness = null,
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
    )

    // --- insertSongs / getSongsByTermPaged ---

    @Test
    fun `insertSongs and getSongsByTermPaged returns songs for term`() = runTest {
        val songs = listOf(buildSong(1L), buildSong(2L))
        db.songDao().insertSongs(songs)

        val result = db.songDao().getSongsByTermPaged(term = "pop", limit = 10, offset = 0)

        assertThat(result).hasSize(2)
    }

    @Test
    fun `getSongsByTermPaged respects limit and offset`() = runTest {
        db.songDao().insertSongs(listOf(buildSong(1L), buildSong(2L), buildSong(3L)))

        val page1 = db.songDao().getSongsByTermPaged("pop", limit = 2, offset = 0)
        val page2 = db.songDao().getSongsByTermPaged("pop", limit = 2, offset = 2)

        assertThat(page1).hasSize(2)
        assertThat(page2).hasSize(1)
    }

    @Test
    fun `getSongsByTermPaged does not return songs for a different term`() = runTest {
        db.songDao().insertSongs(listOf(buildSong(1L, "pop"), buildSong(2L, "rock")))

        val result = db.songDao().getSongsByTermPaged("pop", limit = 10, offset = 0)

        assertThat(result).hasSize(1)
        assertThat(result[0].trackId).isEqualTo(1L)
    }

    @Test
    fun `insertSongs replaces song with same trackId and searchTerm`() = runTest {
        val original = buildSong(1L)
        val updated = original.copy(trackName = "Updated Track")
        db.songDao().insertSongs(listOf(original))
        db.songDao().insertSongs(listOf(updated))

        val result = db.songDao().getSongsByTermPaged("pop", limit = 10, offset = 0)

        assertThat(result).hasSize(1)
        assertThat(result[0].trackName).isEqualTo("Updated Track")
    }

    // --- countSongsByTerm ---

    @Test
    fun `countSongsByTerm returns correct count`() = runTest {
        db.songDao().insertSongs(listOf(buildSong(1L), buildSong(2L), buildSong(3L, "rock")))

        assertThat(db.songDao().countSongsByTerm("pop")).isEqualTo(2)
        assertThat(db.songDao().countSongsByTerm("rock")).isEqualTo(1)
    }

    @Test
    fun `countSongsByTerm returns zero when no songs for term`() = runTest {
        assertThat(db.songDao().countSongsByTerm("jazz")).isEqualTo(0)
    }

    // --- getSongsByAlbumId ---

    @Test
    fun `getSongsByAlbumId returns songs whose albumId and searchTerm match`() = runTest {
        val albumId = 100L
        val albumSong = buildSong(trackId = 1L, searchTerm = albumId.toString(), albumId = albumId)
        val otherSong = buildSong(trackId = 2L, searchTerm = "pop", albumId = albumId)
        db.songDao().insertSongs(listOf(albumSong, otherSong))

        val result = db.songDao().getSongsByAlbumId(albumId.toString())

        assertThat(result).hasSize(1)
        assertThat(result[0]).isEqualTo(albumSong)
    }

    @Test
    fun `getSongsByAlbumId returns empty list when no match`() = runTest {
        db.songDao().insertSongs(listOf(buildSong(1L, "pop")))

        assertThat(db.songDao().getSongsByAlbumId("999")).isEmpty()
    }

    // --- getSongById ---

    @Test
    fun `getSongById returns song with matching trackId`() = runTest {
        db.songDao().insertSongs(listOf(buildSong(42L)))

        val result = db.songDao().getSongById(42L)

        assertThat(result).hasSize(1)
        assertThat(result[0].trackId).isEqualTo(42L)
    }

    @Test
    fun `getSongById returns empty list when trackId not found`() = runTest {
        assertThat(db.songDao().getSongById(99L)).isEmpty()
    }

    // --- deleteSongsByTerm ---

    @Test
    fun `deleteSongsByTerm removes only songs for that term`() = runTest {
        db.songDao().insertSongs(listOf(buildSong(1L, "pop"), buildSong(2L, "rock")))

        db.songDao().deleteSongsByTerm("pop")

        assertThat(db.songDao().countSongsByTerm("pop")).isEqualTo(0)
        assertThat(db.songDao().countSongsByTerm("rock")).isEqualTo(1)
    }

    @Test
    fun `deleteSongsByTerm on non-existent term does not throw`() = runTest {
        db.songDao().deleteSongsByTerm("jazz")

        assertThat(db.songDao().countSongsByTerm("jazz")).isEqualTo(0)
    }
}
