package com.songs.player.data.remote

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.songs.networking.NetworkException
import com.songs.networking.NetworkResponse
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.resources.Resources
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.fail
import org.junit.Test

class PlayerRemoteDataSourceImplTest {

    // -----------------------------------------------------------------------
    // Success path
    // -----------------------------------------------------------------------

    @Test
    fun `getTrack - 200 OK with valid body returns Success with parsed ListTrackResponse`() = runTest {
        // All nullable fields of TrackResponse must be present (even as null) for kotlinx.serialization.
        val json = """
            {
              "resultCount": 1,
              "results": [{
                "wrapperType": "track",
                "kind": "song",
                "artistId": null,
                "collectionId": null,
                "trackId": 42,
                "artistName": null,
                "collectionName": null,
                "trackName": "Test Track",
                "collectionCensoredName": null,
                "trackCensoredName": null,
                "artistViewUrl": null,
                "collectionArtistId": null,
                "collectionArtistViewUrl": null,
                "collectionViewUrl": null,
                "trackViewUrl": null,
                "previewUrl": null,
                "artworkUrl30": null,
                "artworkUrl60": null,
                "artworkUrl100": null,
                "collectionPrice": null,
                "trackPrice": null,
                "trackRentalPrice": null,
                "collectionHdPrice": null,
                "trackHdPrice": null,
                "trackHdRentalPrice": null,
                "releaseDate": null,
                "collectionExplicitness": null,
                "trackExplicitness": null,
                "discCount": null,
                "discNumber": null,
                "trackCount": null,
                "trackNumber": null,
                "trackTimeMillis": null,
                "country": null,
                "currency": null,
                "primaryGenreName": null,
                "contentAdvisoryRating": null,
                "shortDescription": null,
                "longDescription": null,
                "hasITunesExtras": null,
                "isStreamable": null
              }]
            }
        """.trimIndent()
        val client = buildClient(MockEngine { _ ->
            respond(
                content = json,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        })
        client.use {
            val result = PlayerRemoteDataSourceImpl(it).getTrack("42")
            if (result is NetworkResponse.Error) {
                fail("Expected Success but got Error: ${result.exception?.message}")
            }
            assertThat(result).isInstanceOf(NetworkResponse.Success::class)
            val body = (result as NetworkResponse.Success).value
            assertThat(body.resultCount).isEqualTo(1)
            assertThat(body.results[0].trackId).isEqualTo(42L)
            assertThat(body.results[0].trackName).isEqualTo("Test Track")
        }
    }

    @Test
    fun `getTrack - 200 OK with empty results returns Success with empty list`() = runTest {
        val json = """{"resultCount":0,"results":[]}"""
        val client = buildClient(MockEngine { _ ->
            respond(
                content = json,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        })
        client.use {
            val result = PlayerRemoteDataSourceImpl(it).getTrack("99")

            assertThat(result).isInstanceOf(NetworkResponse.Success::class)
            assertThat((result as NetworkResponse.Success).value.results.isEmpty()).isEqualTo(true)
        }
    }

    @Test
    fun `getTrack - sends correct query parameters to the API`() = runTest {
        var capturedId: String? = null
        var capturedEntity: String? = null
        val json = """{"resultCount":0,"results":[]}"""
        val client = buildClient(MockEngine { request ->
            capturedId = request.url.parameters["id"]
            capturedEntity = request.url.parameters["entity"]
            respond(
                content = json,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        })
        client.use {
            PlayerRemoteDataSourceImpl(it).getTrack("42")
        }

        assertThat(capturedId).isEqualTo("42")
        assertThat(capturedEntity).isEqualTo("song")
    }

    // -----------------------------------------------------------------------
    // Non-2xx error path
    // -----------------------------------------------------------------------

    @Test
    fun `getTrack - 404 Not Found returns Error with NetworkException code 404`() = runTest {
        val client = buildClient(MockEngine { _ ->
            respond(content = "", status = HttpStatusCode.NotFound)
        })
        client.use {
            val result = PlayerRemoteDataSourceImpl(it).getTrack("1")

            assertThat(result).isInstanceOf(NetworkResponse.Error::class)
            val exception = (result as NetworkResponse.Error).exception as NetworkException
            assertThat(exception.code).isEqualTo(404)
        }
    }

    @Test
    fun `getTrack - 500 Internal Server Error returns Error with NetworkException code 500`() = runTest {
        val client = buildClient(MockEngine { _ ->
            respond(content = "", status = HttpStatusCode.InternalServerError)
        })
        client.use {
            val result = PlayerRemoteDataSourceImpl(it).getTrack("1")

            assertThat(result).isInstanceOf(NetworkResponse.Error::class)
            assertThat(((result as NetworkResponse.Error).exception as NetworkException).code).isEqualTo(500)
        }
    }

    // -----------------------------------------------------------------------
    // Exception path
    // -----------------------------------------------------------------------

    @Test
    fun `getTrack - engine throws exception returns Error wrapping the cause`() = runTest {
        val client = buildClient(MockEngine { _ ->
            throw RuntimeException("Network unavailable")
        })
        client.use {
            val result = PlayerRemoteDataSourceImpl(it).getTrack("1")

            assertThat(result).isInstanceOf(NetworkResponse.Error::class)
            val exception = (result as NetworkResponse.Error).exception as NetworkException
            assertThat(exception.cause?.message).isEqualTo("Network unavailable")
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private fun buildClient(engine: MockEngine): HttpClient = HttpClient(engine) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                coerceInputValues = true
                explicitNulls = false
            })
        }
        install(Resources)
        defaultRequest {
            url("https://itunes.apple.com/")
        }
    }
}
