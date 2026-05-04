package com.songs.home.data.local

import com.songs.database.entity.AlbumEntity
import com.songs.home.domain.model.AlbumInfo

fun AlbumInfo.toEntity() = AlbumEntity(
    collectionId = collectionId,
    title = title,
    coverUrl = coverUrl,
    artistName = artistName,
)

fun AlbumEntity.toDomain() = AlbumInfo(
    collectionId = collectionId,
    title = title,
    coverUrl = coverUrl,
    artistName = artistName,
)
