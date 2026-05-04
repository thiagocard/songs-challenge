package com.songs.networking.adapter

import com.songs.networking.NetworkException
import com.songs.networking.NetworkResponse
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess

@Suppress("TooGenericExceptionCaught")
suspend inline fun <reified T> safeApiCall(block: () -> HttpResponse): NetworkResponse<T> {
    return try {
        val response = block()
        if (response.status.isSuccess()) {
            NetworkResponse.Success(response.body<T>())
        } else {
            NetworkResponse.Error(
                exception = NetworkException(
                    code = response.status.value,
                    message = response.status.description,
                )
            )
        }
    } catch (e: Exception) {
        NetworkResponse.Error(exception = NetworkException(message = e.message, cause = e))
    }
}
