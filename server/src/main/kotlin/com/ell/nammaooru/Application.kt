package com.ell.nammaooru

import com.ell.nammaooru.data.Response
import com.ell.nammaooru.routes.busRoutes
import com.ell.nammaooru.services.BusService
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.openapi.OpenApiInfo
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.OpenApiDocSource
import io.ktor.server.routing.openapi.hide
import io.ktor.utils.io.ExperimentalKtorApi
import kotlinx.serialization.json.Json
import org.slf4j.event.Level


fun main() {
    embeddedServer(
        Netty,
        host = "0.0.0.0",
        port = 8080,
        module = Application::module
    ).start(wait = true)
}

class UnauthorizedException : RuntimeException()

val ApiKeyPlugin = createRouteScopedPlugin("ApiKeyPlugin") {
    onCall { call ->
        val requestPath = call.request.path()
        if (requestPath == "/swagger" || requestPath.startsWith("/swagger/")) {
            return@onCall
        }

        val apiKey = call.request.headers["X-API-Key"]
        val serverSecret = call.application.environment.config.propertyOrNull("ktor.security.apiKey")?.getString()
            ?: "my-secret-nammaooru-key"

        if (apiKey == null || apiKey != serverSecret) {
            throw UnauthorizedException()
        }
    }
}

@OptIn(ExperimentalKtorApi::class)
fun Application.module() {
    com.ell.nammaooru.database.DatabaseFactory.init(environment.config)

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

//    install(ApiKeyPlugin)

    routing {

        get("/") {
            call.respondText("Hello from Ktor")
        }.hide()

        swaggerUI(path = "swagger") {
            info = OpenApiInfo(
                title = "Namma Ooru Bus API",
                version = "1.0.0",
                description = "Bus timetable and departure-location endpoints."
            )
            source = OpenApiDocSource.Routing(contentType = ContentType.Application.Yaml)
            remotePath = "openapi.yaml"
        }
        busRoutes(busService)
    }
}