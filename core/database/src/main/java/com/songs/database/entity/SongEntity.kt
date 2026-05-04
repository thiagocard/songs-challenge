package com.songs.database.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "songs",
    primaryKeys = ["trackId", "searchTerm"],
    indices = [Index("albumId"), Index("searchTerm")]
)
data class SongEntity(
    val trackId: Long,
    val searchTerm: String,
    val albumId: Long?,
    val wrapperType: String?,
    val kind: String?,
    val collectionId: Long?,
    val artistName: String,
    val collectionName: String?,
    val trackName: String,
    val collectionCensoredName: String?,
    val trackCensoredName: String?,
    val collectionArtistId: Long?,
    val collectionArtistViewUrl: String?,
    val collectionViewUrl: String?,
    val trackViewUrl: String?,
    val previewUrl: String?,
    val artworkUrl30: String?,
    val artworkUrl60: String?,
    val artworkUrl100: String?,
    val collectionPrice: Double?,
    val trackPrice: Double?,
    val trackRentalPrice: Double?,
    val collectionHdPrice: Double?,
    val trackHdPrice: Double?,
    val trackHdRentalPrice: Double?,
    val releaseDate: String?,
    val collectionExplicitness: String?,
    val trackExplicitness: String?,
    val trackCount: Int?,
    val trackNumber: Int?,
    val trackTimeMillis: Long?,
    val country: String?,
    val currency: String?,
    val primaryGenreName: String?,
    val contentAdvisoryRating: String?,
    val shortDescription: String?,
    val longDescription: String?,
    val hasITunesExtras: Boolean?
)
