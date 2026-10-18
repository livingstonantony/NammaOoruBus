package com.ell.nammaooru.services

import org.mindrot.jbcrypt.BCrypt
import com.ell.nammaooru.data.User
import com.ell.nammaooru.database.Users
import com.ell.nammaooru.database.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.UUID

class UserService {

    suspend fun getUserByUsername(username: String): User? = dbQuery {
        Users.selectAll()
            .where { Users.username eq username }
            .map { it.toUser() }
            .singleOrNull()
    }

    suspend fun getUserById(userId: String): User? = dbQuery {
        Users.selectAll()
            .where { Users.id eq userId }
            .map { it.toUser() }
            .singleOrNull()
    }

    suspend fun createUser(username: String, email: String, password: String, isAdmin: Boolean = false): User? = dbQuery {
        try {
            val passwordHash = hashPassword(password)
            val userId = UUID.randomUUID().toString()
            Users.insert {
                it[id] = userId
                it[Users.username] = username
                it[Users.email] = email
                it[Users.passwordHash] = passwordHash
                it[Users.isAdmin] = isAdmin
                it[createdAt] = System.currentTimeMillis()
            }
            User(
                id = userId,
                username = username,
                email = email,
                isAdmin = isAdmin,
                createdAt = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            null
        }
    }

    suspend fun verifyPassword(username: String, password: String): User? = dbQuery {
        Users.selectAll()
            .where { Users.username eq username }
            .map { row ->
                val storedHash = row[Users.passwordHash]
                if (BCrypt.checkpw(password, storedHash)) {
                    row.toUser()
                } else {
                    null
                }
            }
            .singleOrNull()
            ?.let { it }
    }

    private fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt(12))
    }

    private fun ResultRow.toUser(): User = User(
        id = this[Users.id],
        username = this[Users.username],
        email = this[Users.email],
        isAdmin = this[Users.isAdmin],
        createdAt = this[Users.createdAt]
    )
}
