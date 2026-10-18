package com.ell.nammaooru.domain.repository

import com.ell.nammaooru.domain.model.Bus
import com.ell.nammaooru.domain.model.Response

interface BusRepository {

    suspend fun getBuses(): Result<Response<List<Bus>>>

    suspend fun getBus(id: String): Result<Response<Bus>>

    suspend fun createBus(request: Bus): Result<Response<Bus>>

    suspend fun updateBus(id: String, request: Bus): Result<Response<Bus>>

    suspend fun deleteBus(id: String): Result<Response<String?>>
}