package com.songs.home.domain.model

data class PaginationState(
    val currentPage: Int = 0,
    val pageSize: Int = 10,
    val isLoading: Boolean = false,
    val hasMorePages: Boolean = true,
    val totalItems: Int = 0
) {
    val currentOffset: Int
        get() = currentPage * pageSize
}

