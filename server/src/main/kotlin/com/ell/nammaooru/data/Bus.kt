package com.ell.nammaooru.data

import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Instant

@Serializable
data class Bus(
    val id: String = UUID.randomUUID().toString(),
    val destinationEnglish: String,
    val destinationTamil: String,
    val busName: String,
    val note: String,
    val departTime: String,
)
