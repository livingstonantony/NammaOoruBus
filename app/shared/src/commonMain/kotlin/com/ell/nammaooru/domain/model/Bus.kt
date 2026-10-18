package com.ell.nammaooru.domain.model

import kotlinx.serialization.Serializable


@Serializable
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
    val id: String = "",
    val departingFrom: String,
    val destinationEnglish: String,
    val destinationTamil: String,
    val busName: String,
    val note: String,
    val departTime: String,
    val days: List<Days>
)
