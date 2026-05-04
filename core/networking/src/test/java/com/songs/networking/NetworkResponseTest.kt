package com.songs.networking

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class NetworkResponseTest {

    @Test
    fun `toFlow - Success emits the value`() = runTest {
        val response: NetworkResponse<String> = NetworkResponse.Success("hello")

        response.toFlow().test {
            assertThat(awaitItem()).isEqualTo("hello")
            awaitComplete()
        }
    }

    @Test
    fun `toFlow - Success with object emits the object`() = runTest {
        data class Payload(val id: Int, val name: String)
        val payload = Payload(1, "track")
        val response: NetworkResponse<Payload> = NetworkResponse.Success(payload)

        response.toFlow().test {
            assertThat(awaitItem()).isEqualTo(payload)
            awaitComplete()
        }
    }

    @Test
    fun `toFlow - Error with exception rethrows it`() = runTest {
        val cause = NetworkException(code = 404, message = "Not Found")
        val response: NetworkResponse<String> = NetworkResponse.Error(exception = cause)

        response.toFlow().test {
            val thrown = awaitError()
            assertThat(thrown).isInstanceOf(NetworkException::class)
            assertThat((thrown as NetworkException).code).isEqualTo(404)
            assertThat(thrown.message).isEqualTo("Not Found")
        }
    }

    @Test
    fun `toFlow - Error without exception throws generic Exception`() = runTest {
        val response: NetworkResponse<String> = NetworkResponse.Error()

        response.toFlow().test {
            val thrown = awaitError()
            assertThat(thrown).isInstanceOf(Exception::class)
        }
    }

    @Test
    fun `NetworkException - defaults code to -1`() {
        val ex = NetworkException(message = "oops")
        assertThat(ex.code).isEqualTo(-1)
    }

    @Test
    fun `NetworkException - stores code and message`() {
        val ex = NetworkException(code = 500, message = "Internal Server Error")
        assertThat(ex.code).isEqualTo(500)
        assertThat(ex.message).isEqualTo("Internal Server Error")
    }
}
