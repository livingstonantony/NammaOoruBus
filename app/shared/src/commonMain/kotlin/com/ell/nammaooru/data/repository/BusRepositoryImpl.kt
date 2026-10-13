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
import io.ktor.http.ContentType
import io.ktor.http.contentType

class BusRepositoryImpl(private val client: HttpClient) : BusRepository {
    override suspend fun getBuses(): Result<Response<List<Bus>>> {
        return runCatching {
            client.get(ApiConfig.BUSES).body()
        }
    }

    override suspend fun getBus(id: String): Result<Response<Bus>> {
        return runCatching {
            client.get("${ApiConfig.BUSES}/$id").body()
        }
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