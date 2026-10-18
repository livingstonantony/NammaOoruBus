package com.ell.nammaooru.data.repository

import com.ell.nammaooru.domain.model.Response
import com.ell.nammaooru.network.ApiConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable

class AuthRequiredException(message: String = "Admin login required") : Exception(message)

class AuthRepository(private val client: HttpClient) {

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val mutex = Mutex()
    private var guestToken: String? = null
    private var accessToken: String? = null
    private var refreshToken: String? = null

    suspend fun login(username: String, password: String): Result<Unit> = runCatching {
        val response = client.post("${ApiConfig.BASE_API_URL}/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginPayload(username, password))
        }.body<Response<TokenPayload>>()
        val tokens = response.data ?: error(response.message)
        mutex.withLock {
            accessToken = tokens.accessToken
            refreshToken = tokens.refreshToken
        }
        _isLoggedIn.value = true
    }

    suspend fun logout() = mutex.withLock {
        accessToken = null
        refreshToken = null
        _isLoggedIn.value = false
    }

    /** Admin token when logged in, otherwise a guest token. Writes must set [requireAdmin]. */
    suspend fun bearer(requireAdmin: Boolean, forceRefresh: Boolean = false): String = mutex.withLock {
        if (accessToken != null) {
            if (forceRefresh) refreshAdminLocked()
            return@withLock "Bearer $accessToken"
        }
        if (requireAdmin) throw AuthRequiredException()
        if (forceRefresh) guestToken = null
        val token = guestToken ?: fetchGuestToken().also { guestToken = it }
        "Bearer $token"
    }

    private suspend fun fetchGuestToken(): String {
        val response = client.get("${ApiConfig.BASE_API_URL}/auth/guest-token")
            .body<Response<GuestTokenPayload>>()
        return response.data?.accessToken ?: error("Guest token request failed: ${response.message}")
    }

    private suspend fun refreshAdminLocked() {
        val current = refreshToken
        val tokens = runCatching {
            client.post("${ApiConfig.BASE_API_URL}/auth/refresh") {
                contentType(ContentType.Application.Json)
                setBody(RefreshPayload(current.orEmpty()))
            }.body<Response<TokenPayload>>().data
        }.getOrNull()

        if (tokens == null) {
            accessToken = null
            refreshToken = null
            _isLoggedIn.value = false
            throw AuthRequiredException("Session expired. Please log in again.")
        }
        accessToken = tokens.accessToken
        refreshToken = tokens.refreshToken
    }
}

@Serializable
private data class LoginPayload(val username: String, val password: String)

@Serializable
private data class RefreshPayload(val refreshToken: String)

@Serializable
private data class TokenPayload(val accessToken: String, val refreshToken: String)

@Serializable
private data class GuestTokenPayload(val accessToken: String)
