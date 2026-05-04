package com.songs.home.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ListSongsResponse(
    val resultCount: Int,
    val results: List<SongResponse>
)

@Serializable
data class SongResponse(
    val wrapperType: String? = null,
    val kind: String? = null,
    val collectionId: Long? = null,
    val trackId: Long? = null,
    val artistName: String? = null,
    val collectionName: String? = null,
    val trackName: String? = null,
    val collectionCensoredName: String? = null,
    val trackCensoredName: String? = null,
    val collectionArtistId: Long? = null,
    val collectionArtistViewUrl: String? = null,
    val collectionViewUrl: String? = null,
    val trackViewUrl: String? = null,
    val previewUrl: String? = null,
    val artworkUrl30: String? = null,
    val artworkUrl60: String? = null,
    val artworkUrl100: String? = null,
    val collectionPrice: Double? = null,
    val trackPrice: Double? = null,
    val trackRentalPrice: Double? = null,
    val collectionHdPrice: Double? = null,
    val trackHdPrice: Double? = null,
    val trackHdRentalPrice: Double? = null,
    val releaseDate: String? = null,
    val collectionExplicitness: String? = null,
    val trackExplicitness: String? = null,
    val trackCount: Int? = null,
    val trackNumber: Int? = null,
    val trackTimeMillis: Long? = null,
    val country: String? = null,
    val currency: String? = null,
    val primaryGenreName: String? = null,
    val contentAdvisoryRating: String? = null,
    val shortDescription: String? = null,
    val longDescription: String? = null,
    val hasITunesExtras: Boolean? = null
)
