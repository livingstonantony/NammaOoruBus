package com.ell.nammaooru.domain.model

import kotlinx.serialization.Serializable

data class Response<T>(
    val result: String,
    val message: String,
    val data: T?
)
