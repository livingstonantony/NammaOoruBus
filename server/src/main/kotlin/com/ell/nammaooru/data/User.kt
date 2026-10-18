package com.ell.nammaooru.data

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class User(
    val id: String = UUID.randomUUID().toString(),
    val username: String,
    val email: String,
    val isAdmin: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

@Serializable
data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long, // seconds
    val tokenType: String = "Bearer"
)

@Serializable
data class GuestTokenResponse(
    val accessToken: String,
    val expiresIn: Long, // seconds
    val tokenType: String = "Bearer"
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)
