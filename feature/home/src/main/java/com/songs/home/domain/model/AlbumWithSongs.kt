package com.songs.home.domain.model

data class AlbumWithSongs(
    val album: AlbumInfo,
    val songs: List<Song>
)
