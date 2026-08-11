package com.mizbamd.zikra.data.remote

import com.mizbamd.zikra.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class AuthResponse(val token: String, val userId: String, val email: String)

@Serializable
data class ErrorBody(val error: String? = null)

@Serializable
data class Credentials(val email: String, val password: String)

@Serializable
data class FrameDto(
    val id: String,
    val arabic: String,
    val transliteration: String,
    val target: Int? = null,
    val lifetimeCount: Int = 0,
    val sortOrder: Int = 0,
    val createdAt: String,
    val updatedAt: String,
    val deleted: Boolean = false,
)

@Serializable
data class DailyCountDto(
    val id: String,
    val frameId: String,
    val date: String,
    val count: Int,
    val updatedAt: String,
)

@Serializable
data class SyncPullResponse(
    val frames: List<FrameDto> = emptyList(),
    val dailyCounts: List<DailyCountDto> = emptyList(),
)

@Serializable
data class SyncPushRequest(
    val frames: List<FrameDto> = emptyList(),
    val dailyCounts: List<DailyCountDto> = emptyList(),
)

class ZikraApi(private val baseUrl: String = BuildConfig.API_BASE_URL.trimEnd('/')) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val client = HttpClient(OkHttp) {
        expectSuccess = false
        install(HttpTimeout) {
            requestTimeoutMillis = 8_000
            connectTimeoutMillis = 5_000
            socketTimeoutMillis = 8_000
        }
        install(ContentNegotiation) {
            json(json)
        }
        defaultRequest {
            url("$baseUrl/")
            contentType(ContentType.Application.Json)
        }
    }

    suspend fun health(): Boolean = runCatching {
        client.get("$baseUrl/health").status.value == 200
    }.getOrDefault(false)

    suspend fun register(email: String, password: String): AuthResponse =
        authPost("/v1/auth/register", Credentials(email, password))

    suspend fun login(email: String, password: String): AuthResponse =
        authPost("/v1/auth/login", Credentials(email, password))

    suspend fun pull(token: String): SyncPullResponse {
        val res = client.get("$baseUrl/v1/sync") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        return parseOrThrow(res)
    }

    suspend fun push(token: String, body: SyncPushRequest): SyncPullResponse {
        val res = client.post("$baseUrl/v1/sync") {
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(body)
        }
        return parseOrThrow(res)
    }

    suspend fun deleteAccount(token: String) {
        val res = client.delete("$baseUrl/v1/account") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        if (res.status.value in 200..299 || res.status.value == 404) return
        throw ApiException(errorMessage(res))
    }

    private suspend fun authPost(path: String, body: Credentials): AuthResponse {
        val res = client.post("$baseUrl$path") { setBody(body) }
        return parseOrThrow(res)
    }

    private suspend inline fun <reified T> parseOrThrow(res: HttpResponse): T {
        if (res.status.value in 200..299) return res.body()
        throw ApiException(errorMessage(res))
    }

    private suspend fun errorMessage(res: HttpResponse): String {
        val raw = runCatching { res.bodyAsText() }.getOrNull().orEmpty()
        val parsed = runCatching { json.decodeFromString<ErrorBody>(raw).error }.getOrNull()
        return parsed?.takeIf { it.isNotBlank() }
            ?: raw.takeIf { it.isNotBlank() }
            ?: "Request failed (${res.status.value})"
    }
}

class ApiException(message: String) : Exception(message)
