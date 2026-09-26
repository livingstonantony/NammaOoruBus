package com.ell.nammaooru.data

import kotlinx.serialization.Serializable
import java.util.UUID

enum class Days {
    Monday,
    Tuesday,
    Wednesday,
    Thursday,
    Friday,
    Saturday,
    Sunday,
}

@Serializable
data class Bus(
    val id: String = UUID.randomUUID().toString(),
    val departingFrom: String,
    val destinationEnglish: String,
    val destinationTamil: String,
    val busName: String,
    val note: String,
    val departTime: String,
    val days: List<Days>
)
