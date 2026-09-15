package com.ell.nammaooru

import com.ell.nammaooru.routes.busRoutes
import com.ell.nammaooru.services.BusService
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.httpMethod
import io.ktor.server.request.uri
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.slf4j.event.Level


fun main(args: Array<String>): Unit =
    EngineMain.main(args)

fun Application.module() {

    install(CallLogging) {
        level = Level.INFO

        format { call ->
            "${call.request.httpMethod.value} ${call.request.uri} -> ${call.response.status()}"
        }
    }

    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            }
        )
    }

    val busService = BusService()

    routing {

        get("/") {
            call.respondText("Hello from Ktor")
        }

        busRoutes(busService)
    }
}