package com.songs.home.presentation.ui.album

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.songs.home.domain.model.AlbumInfo
import com.songs.home.domain.model.AlbumWithSongs
import com.songs.support.mock.SongMock

class AlbumWithSongsPreviewParameterProvider : PreviewParameterProvider<AlbumWithSongs> {
    override val values = sequenceOf(
        // Pop album – with cover art
        AlbumWithSongs(
            album = AlbumInfo(
                collectionId = 1L,
                title = "Sample Album",
                artistName = "Sample Artist",
                coverUrl = "https://is5-ssl.mzstatic.com/image/thumb/Music125/v4/8b/0c/9e/8b0c9e7a-2d3c-6f1a-7c8e-9b2f5d9a1b2c/source/100x100bb.jpg",
            ),
            songs = listOf(
                SongMock.song.copy(trackId = 1L, trackName = "Track One",   artistName = "Sample Artist",  trackNumber = 1),
                SongMock.song.copy(trackId = 2L, trackName = "Track Two",   artistName = "Sample Artist",  trackNumber = 2),
                SongMock.song.copy(trackId = 3L, trackName = "Track Three", artistName = "Sample Artist",  trackNumber = 3),
            ),
        ),
        // Rock album – no cover art
        AlbumWithSongs(
            album = AlbumInfo(
                collectionId = 2L,
                title = "Another Album",
                artistName = "Another Artist",
                coverUrl = null,
            ),
            songs = listOf(
                SongMock.song.copy(trackId = 10L, trackName = "Rock Song One", artistName = "Another Artist", trackNumber = 1, primaryGenreName = "Rock"),
                SongMock.song.copy(trackId = 11L, trackName = "Rock Song Two", artistName = "Another Artist", trackNumber = 2, primaryGenreName = "Rock"),
            ),
        ),
        // No cover art, unknown metadata
        AlbumWithSongs(
            album = AlbumInfo(
                collectionId = 3L,
                title = "Unknown Album",
                artistName = "Unknown Artist",
                coverUrl = null,
            ),
            songs = listOf(
                SongMock.song.copy(trackId = 20L, trackName = "Unknown Track", artistName = "Unknown Artist", trackNumber = 1),
            ),
        ),
    )
}
