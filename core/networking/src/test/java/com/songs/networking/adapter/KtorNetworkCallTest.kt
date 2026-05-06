package com.songs.networking.adapter

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.songs.networking.NetworkException
import com.songs.networking.NetworkResponse
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Before
import org.junit.Test

class KtorNetworkCallTest {

    @Serializable
    private data class TestPayload(val id: Int, val name: String)

    private lateinit var client: HttpClient

    @Before
    fun setUp() {
        // Engine is replaced per-test; client is rebuilt each time.
    }

    @After
    fun tearDown() {
        if (::client.isInitialized) client.close()
    }

    // -----------------------------------------------------------------------
    // Success path
    // -----------------------------------------------------------------------

    @Test
    fun `safeApiCall - 200 OK returns NetworkResponse Success with parsed body`() = runTest {
        client = buildClient(MockEngine { _ ->
            respond(
                content = """{"id":1,"name":"rock"}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        })

        val result: NetworkResponse<TestPayload> = safeApiCall { client.get("http://localhost/test") }

        assertThat(result).isInstanceOf(NetworkResponse.Success::class)
        val payload = (result as NetworkResponse.Success).value
        assertThat(payload.id).isEqualTo(1)
        assertThat(payload.name).isEqualTo("rock")
    }

    // -----------------------------------------------------------------------
    // Non-2xx error path
    // -----------------------------------------------------------------------

    @Test
    fun `safeApiCall - 404 Not Found returns NetworkResponse Error with code and description`() = runTest {
        client = buildClient(MockEngine { _ ->
            respond(content = "", status = HttpStatusCode.NotFound)
        })

        val result: NetworkResponse<TestPayload> = safeApiCall { client.get("http://localhost/missing") }

        assertThat(result).isInstanceOf(NetworkResponse.Error::class)
        val error = (result as NetworkResponse.Error)
        assertThat(error.exception as Any).isInstanceOf(NetworkException::class)
        assertThat((error.exception as NetworkException).code).isEqualTo(404)
    }

    @Test
    fun `safeApiCall - 500 Internal Server Error returns NetworkResponse Error with code 500`() = runTest {
        client = buildClient(MockEngine { _ ->
            respond(content = "", status = HttpStatusCode.InternalServerError)
        })

        val result: NetworkResponse<TestPayload> = safeApiCall { client.get("http://localhost/error") }

        assertThat(result).isInstanceOf(NetworkResponse.Error::class)
        assertThat(((result as NetworkResponse.Error).exception as NetworkException).code).isEqualTo(500)
    }

    // -----------------------------------------------------------------------
    // Exception path
    // -----------------------------------------------------------------------

    @Test
    fun `safeApiCall - engine throws exception returns NetworkResponse Error wrapping the cause`() = runTest {
        client = buildClient(MockEngine { _ ->
            throw RuntimeException("Connection refused")
        })

        val result: NetworkResponse<TestPayload> = safeApiCall { client.get("http://localhost/crash") }

        assertThat(result).isInstanceOf(NetworkResponse.Error::class)
        val exception = (result as NetworkResponse.Error).exception
        assertThat(exception as Any).isInstanceOf(NetworkException::class)
        assertThat((exception as NetworkException).cause?.message).isEqualTo("Connection refused")
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private fun buildClient(engine: MockEngine): HttpClient = HttpClient(engine) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
}
