package com.songs.networking

data class NetworkException(
    val code: Int? = -1,
    override val message: String? = null,
    override val cause: Throwable? = null,
) : Exception(message, cause)