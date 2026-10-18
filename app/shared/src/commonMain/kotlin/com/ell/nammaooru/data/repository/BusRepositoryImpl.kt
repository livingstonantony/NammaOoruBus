package com.ell.nammaooru.data.repository

import com.ell.nammaooru.domain.model.Bus
import com.ell.nammaooru.domain.model.Response
import com.ell.nammaooru.domain.repository.BusRepository
import com.ell.nammaooru.network.ApiConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

class BusRepositoryImpl(
    private val client: HttpClient,
    private val auth: AuthRepository
) : BusRepository {

    override suspend fun getBuses(): Result<Response<List<Bus>>> =
        runCatching { send(HttpMethod.Get, ApiConfig.BUSES, requireAdmin = false) }

    override suspend fun getBus(id: String): Result<Response<Bus>> =
        runCatching { send(HttpMethod.Get, "${ApiConfig.BUSES}/$id", requireAdmin = false) }

    override suspend fun createBus(request: Bus): Result<Response<Bus>> =
        runCatching { send(HttpMethod.Post, ApiConfig.BUSES, requireAdmin = true, payload = request) }

    override suspend fun updateBus(id: String, request: Bus): Result<Response<Bus>> =
        runCatching { send(HttpMethod.Put, "${ApiConfig.BUSES}/$id", requireAdmin = true, payload = request) }

    override suspend fun deleteBus(id: String): Result<Response<String?>> =
        runCatching { send(HttpMethod.Delete, "${ApiConfig.BUSES}/$id", requireAdmin = true) }

    private suspend inline fun <reified T> send(
        method: HttpMethod,
        url: String,
        requireAdmin: Boolean,
        payload: Bus? = null
    ): Response<T> {
        val body = execute(method, url, requireAdmin, payload).body<Response<T>>()
        if (body.result != "success") error(body.message)
        return body
    }

    private suspend fun execute(
        method: HttpMethod,
        url: String,
        requireAdmin: Boolean,
        payload: Bus?
    ): HttpResponse {
        suspend fun attempt(forceRefresh: Boolean): HttpResponse {
            val authorization = auth.bearer(requireAdmin, forceRefresh)
            return client.request(url) {
                this.method = method
                header(HttpHeaders.Authorization, authorization)
                if (payload != null) {
                    contentType(ContentType.Application.Json)
                    setBody(payload)
                }
            }
        }

        var response = attempt(forceRefresh = false)
        if (response.status == HttpStatusCode.Unauthorized) {
            response = attempt(forceRefresh = true)
        }
        if (response.status == HttpStatusCode.Unauthorized) {
            throw AuthRequiredException("Not authorized. Please log in again.")
        }
        return response
    }
}
