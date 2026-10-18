package com.ell.nammaooru

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.ell.nammaooru.data.Response
import com.ell.nammaooru.routes.authRoutes
import com.ell.nammaooru.routes.busRoutes
import com.ell.nammaooru.services.AuthService
import com.ell.nammaooru.services.UserService
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.openapi.OpenApiInfo
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.OpenApiDocSource
import io.ktor.server.routing.openapi.hide
import io.ktor.utils.io.ExperimentalKtorApi
import kotlinx.coroutines.launch
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

@OptIn(ExperimentalKtorApi::class)
fun Application.module() {
    com.ell.nammaooru.database.DatabaseFactory.init(environment.config)

    val jwtSecret = environment.config.propertyOrNull("ktor.security.jwt.secret")?.getString()
        ?: "your-super-secret-jwt-key-change-in-production"
    val jwtIssuer = environment.config.propertyOrNull("ktor.security.jwt.issuer")?.getString()
        ?: "nammaooru-api"
    val bootstrapAdminUsername = environment.config.propertyOrNull("ktor.security.bootstrap.adminUsername")?.getString()
        ?: "admin"
    val bootstrapAdminEmail = environment.config.propertyOrNull("ktor.security.bootstrap.adminEmail")?.getString()
        ?: "admin@nammaooru.local"
    val bootstrapAdminPassword = environment.config.propertyOrNull("ktor.security.bootstrap.adminPassword")?.getString()
        ?: "admin123"

    val userService = UserService()
    val authService = AuthService(jwtSecret, jwtIssuer)

    // Initialize bootstrap admin
    this.launch {
        val adminExists = userService.getUserByUsername(bootstrapAdminUsername) != null
        if (!adminExists) {
            userService.createUser(bootstrapAdminUsername, bootstrapAdminEmail, bootstrapAdminPassword, isAdmin = true)
        }
    }

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

    install(Authentication) {
        jwt("auth-jwt") {
            realm = "Namma Ooru Bus API"
            verifier(
                JWT.require(Algorithm.HMAC256(jwtSecret))
                    .withIssuer(jwtIssuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.expiresAt.time > System.currentTimeMillis()) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { defaultScheme, realm ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    Response(
                        result = "failed",
                        message = "Token is not valid or has expired",
                        data = null
                    )
                )
            }
        }
    }

    routing {

        get("/") {
            call.respondText("Hello from Ktor")
        }.hide()

        swaggerUI(path = "swagger") {
            info = OpenApiInfo(
                title = "Namma Ooru Bus API",
                version = "1.0.0",
                description = "Bus timetable and departure-location endpoints with JWT authentication."
            )
            source = OpenApiDocSource.Routing(contentType = ContentType.Application.Yaml)
            remotePath = "openapi.yaml"
        }

        // Public auth routes (no authentication required)
        authRoutes(authService, userService)
        busRoutes(com.ell.nammaooru.services.BusService())
    }
}