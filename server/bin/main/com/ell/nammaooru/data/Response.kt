package com.ell.nammaooru.data

import kotlinx.serialization.Serializable

@Serializable
data class Response<T>(
    val result: String,
    val message: String,
    val data: T?
)
