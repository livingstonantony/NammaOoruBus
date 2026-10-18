package com.ell.nammaooru.routes

import com.ell.nammaooru.data.*
import com.ell.nammaooru.services.AuthService
import com.ell.nammaooru.services.UserService
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.openapi.jsonSchema
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.openapi.describe
import io.ktor.utils.io.ExperimentalKtorApi

@OptIn(ExperimentalKtorApi::class)
fun Route.authRoutes(authService: AuthService, userService: UserService) {
    route("/api/v1/auth") {
        // Guest token endpoint - public, no auth required
        get("/guest-token") {
            val token = authService.generateGuestAccessToken()
            call.respond(
                Response(
                    result = "success",
                    message = "Guest token generated successfully",
                    data = GuestTokenResponse(
                        accessToken = token,
                        expiresIn = 15 * 60, // 15 minutes in seconds
                        tokenType = "Bearer"
                    )
                )
            )
        }.describe {
            summary = "Get guest access token"
            tag("Authentication")
            responses {
                HttpStatusCode.OK {
                    description = "Guest access token generated (read-only, 15-minute lifetime)."
                    ContentType.Application.Json {
                        schema = jsonSchema<Response<GuestTokenResponse>>()
                    }
                }
            }
        }

        // Register endpoint - public
        post("/register") {
            try {
                val request = call.receive<RegisterRequest>()

                if (request.username.isBlank() || request.email.isBlank() || request.password.isBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        Response(
                            result = "failed",
                            message = "Username, email, and password are required",
                            data = null
                        )
                    )
                    return@post
                }

                if (userService.getUserByUsername(request.username) != null) {
                    call.respond(
                        HttpStatusCode.Conflict,
                        Response(
                            result = "failed",
                            message = "Username already exists",
                            data = null
                        )
                    )
                    return@post
                }

                val user = userService.createUser(request.username, request.email, request.password)
                if (user == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        Response(
                            result = "failed",
                            message = "Failed to create user",
                            data = null
                        )
                    )
                    return@post
                }

                val accessToken = authService.generateAccessToken(user)
                val refreshToken = authService.generateRefreshToken(user.id)

                call.respond(
                    HttpStatusCode.Created,
                    Response(
                        result = "success",
                        message = "User registered successfully",
                        data = TokenResponse(
                            accessToken = accessToken,
                            refreshToken = refreshToken,
                            expiresIn = 15 * 60 // 15 minutes in seconds
                        )
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    Response(
                        result = "failed",
                        message = "Registration failed: ${e.message}",
                        data = null
                    )
                )
            }
        }.describe {
            summary = "Register new user"
            tag("Authentication")
            requestBody {
                required = true
                description = "User registration credentials."
                ContentType.Application.Json {
                    schema = jsonSchema<RegisterRequest>()
                }
            }
            responses {
                HttpStatusCode.Created {
                    description = "User registered successfully with tokens."
                    ContentType.Application.Json {
                        schema = jsonSchema<Response<TokenResponse>>()
                    }
                }
                HttpStatusCode.BadRequest {
                    description = "Invalid registration request."
                }
                HttpStatusCode.Conflict {
                    description = "Username already exists."
                }
            }
        }

        // Login endpoint - public
        post("/login") {
            try {
                val request = call.receive<LoginRequest>()

                val user = userService.verifyPassword(request.username, request.password)
                if (user == null) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        Response(
                            result = "failed",
                            message = "Invalid username or password",
                            data = null
                        )
                    )
                    return@post
                }

                val accessToken = authService.generateAccessToken(user)
                val refreshToken = authService.generateRefreshToken(user.id)

                call.respond(
                    Response(
                        result = "success",
                        message = "Login successful",
                        data = TokenResponse(
                            accessToken = accessToken,
                            refreshToken = refreshToken,
                            expiresIn = 15 * 60 // 15 minutes in seconds
                        )
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    Response(
                        result = "failed",
                        message = "Login failed: ${e.message}",
                        data = null
                    )
                )
            }
        }.describe {
            summary = "Login with credentials"
            tag("Authentication")
            requestBody {
                required = true
                description = "Login credentials."
                ContentType.Application.Json {
                    schema = jsonSchema<LoginRequest>()
                }
            }
            responses {
                HttpStatusCode.OK {
                    description = "Login successful with tokens."
                    ContentType.Application.Json {
                        schema = jsonSchema<Response<TokenResponse>>()
                    }
                }
                HttpStatusCode.Unauthorized {
                    description = "Invalid credentials."
                }
                HttpStatusCode.BadRequest {
                    description = "Invalid login request."
                }
            }
        }

        // Refresh token endpoint - requires valid refresh token
        post("/refresh") {
            try {
                val request = call.receive<RefreshTokenRequest>()

                val tokens = authService.verifyAndRefreshToken(request.refreshToken)
                if (tokens == null) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        Response(
                            result = "failed",
                            message = "Invalid or expired refresh token",
                            data = null
                        )
                    )
                    return@post
                }

                call.respond(
                    Response(
                        result = "success",
                        message = "Token refreshed successfully",
                        data = TokenResponse(
                            accessToken = tokens.first,
                            refreshToken = tokens.second,
                            expiresIn = 15 * 60 // 15 minutes in seconds
                        )
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    Response(
                        result = "failed",
                        message = "Token refresh failed: ${e.message}",
                        data = null
                    )
                )
            }
        }.describe {
            summary = "Refresh access token"
            tag("Authentication")
            requestBody {
                required = true
                description = "Refresh token."
                ContentType.Application.Json {
                    schema = jsonSchema<RefreshTokenRequest>()
                }
            }
            responses {
                HttpStatusCode.OK {
                    description = "New tokens generated."
                    ContentType.Application.Json {
                        schema = jsonSchema<Response<TokenResponse>>()
                    }
                }
                HttpStatusCode.Unauthorized {
                    description = "Invalid or expired refresh token."
                }
                HttpStatusCode.BadRequest {
                    description = "Invalid refresh request."
                }
            }
        }
    }
}
