package com.songs.home.domain.mapper

import com.songs.common.resource.ResourceProvider
import com.songs.feature.home.R
import com.songs.home.data.model.ListSongsResponse
import com.songs.home.domain.model.Song
import javax.inject.Inject

interface SongsMapper {
    fun map(listSongsResponse: ListSongsResponse): List<Song>
}

class SongsMapperImpl @Inject constructor(
    private val resourceProvider: ResourceProvider,
) : SongsMapper {
    override fun map(listSongsResponse: ListSongsResponse): List<Song> {
        return listSongsResponse.results.map { response ->
            Song(
                wrapperType = response.wrapperType,
                kind = response.kind,
                collectionId = response.collectionId,
                trackId = response.trackId,
                artistName = response.artistName
                    ?: resourceProvider.getString(R.string.unknown_artist),
                collectionName = response.collectionName,
                trackName = response.trackName ?: resourceProvider.getString(R.string.unknown),
                collectionCensoredName = response.collectionCensoredName,
                trackCensoredName = response.trackCensoredName,
                collectionArtistId = response.collectionArtistId,
                collectionArtistViewUrl = response.collectionArtistViewUrl,
                collectionViewUrl = response.collectionViewUrl,
                trackViewUrl = response.trackViewUrl,
                previewUrl = response.previewUrl,
                artworkUrl30 = response.artworkUrl30,
                artworkUrl60 = response.artworkUrl60,
                artworkUrl100 = response.artworkUrl100,
                collectionPrice = response.collectionPrice,
                trackPrice = response.trackPrice,
                trackRentalPrice = response.trackRentalPrice,
                collectionHdPrice = response.collectionHdPrice,
                trackHdPrice = response.trackHdPrice,
                trackHdRentalPrice = response.trackHdRentalPrice,
                releaseDate = response.releaseDate,
                collectionExplicitness = response.collectionExplicitness,
                trackExplicitness = response.trackExplicitness,
                trackCount = response.trackCount,
                trackNumber = response.trackNumber,
                trackTimeMillis = response.trackTimeMillis,
                country = response.country,
                currency = response.currency,
                primaryGenreName = response.primaryGenreName,
                contentAdvisoryRating = response.contentAdvisoryRating,
                shortDescription = response.shortDescription,
                longDescription = response.longDescription,
                hasITunesExtras = response.hasITunesExtras
            )
        }
    }
}
