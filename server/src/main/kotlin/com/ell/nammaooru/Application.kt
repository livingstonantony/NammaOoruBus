package com.ell.nammaooru

import com.ell.nammaooru.data.Response
import com.ell.nammaooru.routes.busRoutes
import com.ell.nammaooru.services.BusService
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.slf4j.event.Level


fun main(args: Array<String>): Unit =
    EngineMain.main(args)

class UnauthorizedException : RuntimeException()

val ApiKeyPlugin = createRouteScopedPlugin("ApiKeyPlugin") {
    onCall { call ->
        val apiKey = call.request.headers["X-API-Key"]
        val serverSecret = call.application.environment.config.propertyOrNull("ktor.security.apiKey")?.getString()

        if (apiKey == null || apiKey != serverSecret) {
            throw UnauthorizedException()
        }
    }
}

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

    install(StatusPages) {
        exception<UnauthorizedException> { call, _ ->
            call.respond(
                HttpStatusCode.Unauthorized,
                Response(
                    result = "failed",
                    message = "Unauthorized access",
                    data = null
                )
            )
        }
    }

    val busService = BusService()

    install(ApiKeyPlugin)

    routing {

        get("/") {
            call.respondText("Hello from Ktor")
        }

        busRoutes(busService)
    }
}