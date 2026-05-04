package com.songs.home.data.remote

import com.songs.home.loadJson
import com.songs.networking.NetworkResponse
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import org.junit.Test

class SongsRemoteDataSourceImplTest {

    private val validSongsJson = loadJson("songs_valid.json")
    private val emptyResultJson = loadJson("songs_empty.json")

    private fun buildDataSource(engine: MockEngine): SongsRemoteDataSourceImpl {
        val client = HttpClient(engine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        return SongsRemoteDataSourceImpl(client)
    }

    private fun jsonHeaders() = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())

    // -----------------------------------------------------------------------
    // getSongs
    // -----------------------------------------------------------------------

    @Test
    fun `getSongs - returns Success with parsed songs on 200`() = runTest {
        val engine = MockEngine { respond(validSongsJson, HttpStatusCode.OK, jsonHeaders()) }
        val dataSource = buildDataSource(engine)

        val result = dataSource.getSongs(term = "pop")

        assertThat(result).isInstanceOf(NetworkResponse.Success::class)
        val songs = (result as NetworkResponse.Success).value
        assertThat(songs.resultCount).isEqualTo(2)
        assertThat(songs.results.size).isEqualTo(2)
        assertThat(songs.results[0].artistName).isEqualTo("Sample Artist")
    }

    @Test
    fun `getSongs - sends correct query parameters`() = runTest {
        val engine = MockEngine { request ->
            val url = request.url
            assertThat(url.encodedPath.trimStart('/')).isEqualTo("search")
            assertThat(url.parameters["entity"]).isEqualTo("song")
            assertThat(url.parameters["term"]).isEqualTo("rock")
            assertThat(url.parameters["limit"]).isEqualTo("5")
            assertThat(url.parameters["offset"]).isEqualTo("10")
            respond(emptyResultJson, HttpStatusCode.OK, jsonHeaders())
        }
        val dataSource = buildDataSource(engine)

        dataSource.getSongs(term = "rock", limit = 5, offset = 10)
    }

    @Test
    fun `getSongs - returns Success with empty list on empty response`() = runTest {
        val engine = MockEngine { respond(emptyResultJson, HttpStatusCode.OK, jsonHeaders()) }
        val dataSource = buildDataSource(engine)

        val result = dataSource.getSongs(term = "pop")

        assertThat(result).isInstanceOf(NetworkResponse.Success::class)
        assertThat((result as NetworkResponse.Success).value.resultCount).isEqualTo(0)
    }

    @Test
    fun `getSongs - returns Error on HTTP 500`() = runTest {
        val engine = MockEngine { respondError(HttpStatusCode.InternalServerError) }
        val dataSource = buildDataSource(engine)

        val result = dataSource.getSongs(term = "pop")

        assertThat(result).isInstanceOf(NetworkResponse.Error::class)
    }

    @Test
    fun `getSongs - returns Error on HTTP 404`() = runTest {
        val engine = MockEngine { respondError(HttpStatusCode.NotFound) }
        val dataSource = buildDataSource(engine)

        val result = dataSource.getSongs(term = "pop")

        assertThat(result).isInstanceOf(NetworkResponse.Error::class)
    }

    @Test
    fun `getSongs - returns Error when network throws exception`() = runTest {
        val engine = MockEngine { throw RuntimeException("Connection refused") }
        val dataSource = buildDataSource(engine)

        val result = dataSource.getSongs(term = "pop")

        assertThat(result).isInstanceOf(NetworkResponse.Error::class)
    }

    @Test
    fun `getSongs - uses default limit and offset when not provided`() = runTest {
        val engine = MockEngine { request ->
            assertThat(request.url.parameters["limit"]).isEqualTo("10")
            assertThat(request.url.parameters["offset"]).isEqualTo("0")
            respond(emptyResultJson, HttpStatusCode.OK, jsonHeaders())
        }
        val dataSource = buildDataSource(engine)

        dataSource.getSongs(term = "pop")
    }

    // -----------------------------------------------------------------------
    // getSongsByCollectionId
    // -----------------------------------------------------------------------

    @Test
    fun `getSongsByCollectionId - returns Success with parsed songs on 200`() = runTest {
        val engine = MockEngine { respond(validSongsJson, HttpStatusCode.OK, jsonHeaders()) }
        val dataSource = buildDataSource(engine)

        val result = dataSource.getSongsByCollectionId(collectionId = "123")

        assertThat(result).isInstanceOf(NetworkResponse.Success::class)
        val songs = (result as NetworkResponse.Success).value
        assertThat(songs.results.size).isEqualTo(2)
    }

    @Test
    fun `getSongsByCollectionId - sends correct query parameters`() = runTest {
        val engine = MockEngine { request ->
            val url = request.url
            assertThat(url.encodedPath.trimStart('/')).isEqualTo("lookup")
            assertThat(url.parameters["entity"]).isEqualTo("song")
            assertThat(url.parameters["id"]).isEqualTo("123")
            respond(emptyResultJson, HttpStatusCode.OK, jsonHeaders())
        }
        val dataSource = buildDataSource(engine)

        dataSource.getSongsByCollectionId(collectionId = "123")
    }

    @Test
    fun `getSongsByCollectionId - returns Error on HTTP 500`() = runTest {
        val engine = MockEngine { respondError(HttpStatusCode.InternalServerError) }
        val dataSource = buildDataSource(engine)

        val result = dataSource.getSongsByCollectionId(collectionId = "123")

        assertThat(result).isInstanceOf(NetworkResponse.Error::class)
    }

    @Test
    fun `getSongsByCollectionId - returns Error when network throws exception`() = runTest {
        val engine = MockEngine { throw RuntimeException("Timeout") }
        val dataSource = buildDataSource(engine)

        val result = dataSource.getSongsByCollectionId(collectionId = "123")

        assertThat(result).isInstanceOf(NetworkResponse.Error::class)
    }

    @Test
    fun `getSongsByCollectionId - returns Success with empty results`() = runTest {
        val engine = MockEngine { respond(emptyResultJson, HttpStatusCode.OK, jsonHeaders()) }
        val dataSource = buildDataSource(engine)

        val result = dataSource.getSongsByCollectionId(collectionId = "999")

        assertThat(result).isInstanceOf(NetworkResponse.Success::class)
        assertThat((result as NetworkResponse.Success).value.results.size).isEqualTo(0)
    }
}
