package com.ell.nammaooru.services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.ell.nammaooru.data.User
import com.ell.nammaooru.database.RefreshTokens
import com.ell.nammaooru.database.Users
import com.ell.nammaooru.database.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import java.security.MessageDigest
import java.util.*

class AuthService(private val jwtSecret: String, private val jwtIssuer: String) {

    private val algorithm = Algorithm.HMAC256(jwtSecret)

    // Access token: 15 minutes
    private val accessTokenExpiry = 15 * 60 * 1000L

    // Refresh token: 30 days
    private val refreshTokenExpiry = 30 * 24 * 60 * 60 * 1000L

    fun generateAccessToken(user: User): String {
        return JWT.create()
            .withIssuer(jwtIssuer)
            .withSubject(user.id)
            .withClaim("username", user.username)
            .withClaim("isAdmin", user.isAdmin)
            .withExpiresAt(Date(System.currentTimeMillis() + accessTokenExpiry))
            .sign(algorithm)
    }

    fun generateGuestAccessToken(): String {
        return JWT.create()
            .withIssuer(jwtIssuer)
            .withSubject("guest")
            .withClaim("username", "guest")
            .withClaim("isAdmin", false)
            .withExpiresAt(Date(System.currentTimeMillis() + accessTokenExpiry))
            .sign(algorithm)
    }

    suspend fun generateRefreshToken(userId: String): String = dbQuery {
        val tokenId = UUID.randomUUID().toString()
        val token = JWT.create()
            .withIssuer(jwtIssuer)
            .withSubject(userId)
            .withClaim("tokenId", tokenId)
            .withExpiresAt(Date(System.currentTimeMillis() + refreshTokenExpiry))
            .sign(algorithm)

        RefreshTokens.insert {
            it[id] = tokenId
            it[RefreshTokens.userId] = userId
            it[RefreshTokens.token] = hashRefreshToken(token)
            it[expiresAt] = System.currentTimeMillis() + refreshTokenExpiry
            it[createdAt] = System.currentTimeMillis()
        }

        token
    }

    fun verifyAccessToken(token: String): User? {
        return try {
            val decodedJWT = JWT.require(algorithm)
                .withIssuer(jwtIssuer)
                .build()
                .verify(token)

            val userId = decodedJWT.subject
            val username = decodedJWT.getClaim("username").asString()
            val isAdmin = decodedJWT.getClaim("isAdmin").asBoolean()

            if (userId == "guest") {
                // Guest token
                User(
                    id = "guest",
                    username = "guest",
                    email = "guest@example.com",
                    isAdmin = false
                )
            } else {
                User(
                    id = userId,
                    username = username,
                    email = "",
                    isAdmin = isAdmin
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun verifyAndRefreshToken(refreshToken: String): Pair<String, String>? = dbQuery {
        val decodedJWT = runCatching {
            JWT.require(algorithm)
                .withIssuer(jwtIssuer)
                .build()
                .verify(refreshToken)
        }.getOrNull() ?: return@dbQuery null

        val userId = decodedJWT.subject
        val tokenId = decodedJWT.getClaim("tokenId").asString()
        if (userId.isNullOrBlank() || tokenId.isNullOrBlank()) return@dbQuery null

        val storedToken = RefreshTokens.selectAll()
            .where { (RefreshTokens.userId eq userId) and (RefreshTokens.id eq tokenId) }
            .firstOrNull() ?: return@dbQuery null
        val storedHash = storedToken[RefreshTokens.token]
        if (storedToken[RefreshTokens.expiresAt] <= System.currentTimeMillis() ||
            !MessageDigest.isEqual(
                storedHash.toByteArray(Charsets.US_ASCII),
                hashRefreshToken(refreshToken).toByteArray(Charsets.US_ASCII)
            )
        ) {
            return@dbQuery null
        }

        val userRow = Users.selectAll()
            .where { Users.id eq userId }
            .firstOrNull() ?: return@dbQuery null
        val user = User(
            id = userRow[Users.id],
            username = userRow[Users.username],
            email = userRow[Users.email],
            isAdmin = userRow[Users.isAdmin],
            createdAt = userRow[Users.createdAt]
        )

        val consumedCount = RefreshTokens.deleteWhere {
            (RefreshTokens.id eq tokenId) and (RefreshTokens.userId eq userId)
        }
        if (consumedCount != 1) return@dbQuery null

        val newRefreshTokenId = UUID.randomUUID().toString()
        val newRefreshToken = JWT.create()
            .withIssuer(jwtIssuer)
            .withSubject(userId)
            .withClaim("tokenId", newRefreshTokenId)
            .withExpiresAt(Date(System.currentTimeMillis() + refreshTokenExpiry))
            .sign(algorithm)

        RefreshTokens.insert {
            it[id] = newRefreshTokenId
            it[RefreshTokens.userId] = userId
            it[RefreshTokens.token] = hashRefreshToken(newRefreshToken)
            it[expiresAt] = System.currentTimeMillis() + refreshTokenExpiry
            it[createdAt] = System.currentTimeMillis()
        }

        Pair(generateAccessToken(user), newRefreshToken)
    }

    suspend fun revokeRefreshToken(userId: String) = dbQuery {
        RefreshTokens.deleteWhere { RefreshTokens.userId eq userId }
    }

    private fun hashRefreshToken(token: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(token.toByteArray(Charsets.UTF_8))
            .joinToString("") { it.toUByte().toString(16).padStart(2, '0') }
}
