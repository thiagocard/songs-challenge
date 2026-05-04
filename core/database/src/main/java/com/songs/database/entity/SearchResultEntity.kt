package com.songs.database.entity

import androidx.room.Entity
import androidx.room.Index

/**
 * Ordered reference table that mirrors the iTunes API response for a search term.
 *
 * The songs table stays deduplicated (keyed by trackId + searchTerm).
 * This table records every position the API returned, so the same [trackId]
 * can appear at multiple positions — exactly as iTunes returned it.
 */
@Entity(
    tableName = "search_results",
    primaryKeys = ["searchTerm", "position"],
    indices = [Index("searchTerm"), Index("trackId")],
)
data class SearchResultEntity(
    val searchTerm: String,
    /** Zero-based index of this entry in the full API result set for [searchTerm]. */
    val position: Int,
    val trackId: Long,
)
