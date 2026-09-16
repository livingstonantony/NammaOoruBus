package com.ell.nammaooru.data

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Bus(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val routeNumber: String
)
