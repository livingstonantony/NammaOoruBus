package com.ell.nammaooru.data.repository

import com.ell.nammaooru.domain.model.Bus
import com.ell.nammaooru.domain.model.Response
import com.ell.nammaooru.domain.repository.BusRepository
import com.ell.nammaooru.network.ApiConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.put
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

class BusRepositoryImpl(private val client: HttpClient) : BusRepository {
    override suspend fun getBuses(): Result<Response<List<Bus>>> {
        return runCatching {
            val authorization = guestAuthorization()
            client.get(ApiConfig.BUSES) {
                header(HttpHeaders.Authorization, authorization)
            }.body()
        }
    }

    override suspend fun getBus(id: String): Result<Response<Bus>> {
        return runCatching {
            val authorization = guestAuthorization()
            client.get("${ApiConfig.BUSES}/$id") {
                header(HttpHeaders.Authorization, authorization)
            }.body()
        }
    }

    private suspend fun guestAuthorization(): String {
        val response = client.get("${ApiConfig.BASE_API_URL}/auth/guest-token")
            .body<Response<GuestTokenPayload>>()
        val accessToken = response.data?.accessToken
            ?: error("Guest token request failed: ${response.message}")
        return "Bearer $accessToken"
    }

    override suspend fun createBus(request: Bus): Result<Response<Bus>> {
        return runCatching {
            client.post(ApiConfig.BUSES) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        }
    }

    override suspend fun updateBus(
        id: String,
        request: Bus
    ): Result<Response<Bus>> {
        return runCatching {
            client.put("${ApiConfig.BUSES}/$id") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        }
    }

    override suspend fun deleteBus(id: String): Result<Response<Any?>> {
        return runCatching {
            client.delete("${ApiConfig.BUSES}/$id").body()
        }
    }


}

@Serializable
private data class GuestTokenPayload(val accessToken: String)