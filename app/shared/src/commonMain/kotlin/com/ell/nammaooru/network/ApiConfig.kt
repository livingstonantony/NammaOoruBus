package com.ell.nammaooru.network


object ApiConfig {

//    const val BASE_URL = "http://10.0.2.2:8080"
//    const val BASE_URL = "http://localhost:8080"
    const val BASE_URL = "http://127.0.0.1:8080"

    const val API_VERSION = "api/v1"

    const val BASE_API_URL = "$BASE_URL/$API_VERSION"

    const val BUSES = "$BASE_API_URL/buses"
}