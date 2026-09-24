package com.ell.nammaooru.database

import com.ell.nammaooru.data.Days
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import jdk.jfr.internal.event.EventConfiguration.timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.json.jsonb
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    private var initialized = false

    fun init(config: ApplicationConfig) {
        if (initialized) return
        initialized = true
        val driverClass = config.propertyOrNull("ktor.database.driverClassName")?.getString()
            ?: "com.mysql.cj.jdbc.Driver"
        val jdbcUrl = config.propertyOrNull("ktor.database.jdbcUrl")?.getString()
            ?: "jdbc:mysql://localhost:3306/nammaooru?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true"
        val username = config.propertyOrNull("ktor.database.username")?.getString()
            ?: "root"
        val password = config.propertyOrNull("ktor.database.password")?.getString()
            ?: "root"

        try {
            val hikariConfig = HikariConfig().apply {
                driverClassName = driverClass
                this.jdbcUrl = jdbcUrl
                this.username = username
                this.password = password
                maximumPoolSize = 3
                isAutoCommit = false
                transactionIsolation = "TRANSACTION_REPEATABLE_READ"
                validate()
            }
            val dataSource = HikariDataSource(hikariConfig)
            Database.connect(dataSource)

            transaction {
                SchemaUtils.create(Buses)
            }
        } catch (e: Exception) {
            System.err.println("Database initialization failed: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}

object Buses : Table("buses") {
    val id = varchar("id", 50)
    val destinationEnglish = varchar("destination_english", 255)
    val destinationTamil = varchar("destination_tamil", 255)
    val busName = varchar("bus_name", 50)
    val note = varchar("note", 50)
    val departTime = varchar("departTime", 50)

    val days = jsonb<List<Days>>(
        name = "days",
        serialize = { Json.encodeToString(it) },
        deserialize = { Json.decodeFromString(it) }
    )

    override val primaryKey = PrimaryKey(id)
}
