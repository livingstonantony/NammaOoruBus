package com.ell.nammaooru

import com.ell.nammaooru.data.Bus
import com.ell.nammaooru.data.Days
import com.ell.nammaooru.data.GuestTokenResponse
import com.ell.nammaooru.data.LoginRequest
import com.ell.nammaooru.data.RefreshTokenRequest
import com.ell.nammaooru.data.RegisterRequest
import com.ell.nammaooru.data.Response
import com.ell.nammaooru.data.TokenResponse
import com.ell.nammaooru.database.DatabaseFactory
import com.ell.nammaooru.database.RefreshTokens
import com.ell.nammaooru.database.Users
import com.ell.nammaooru.services.BusService
import com.ell.nammaooru.services.UserService
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.Application
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.testing.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import kotlin.test.*
import java.util.UUID

class ApplicationTest {

    @Test
    fun testRoot() = testApplication {
        application {
            module()
        }
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Hello from Ktor", response.bodyAsText())
    }

    @Test
    fun testOpenApiDocumentAndSwaggerUiAreAvailableWithoutApiKey() = testApplication {
        application {
            module()
        }

        val documentResponse = client.get("/swagger/openapi.yaml")
        assertEquals(HttpStatusCode.OK, documentResponse.status)
        val document = documentResponse.bodyAsText()
        assertContains(document, "openapi:")
        assertContains(document, "/api/v1/buses:")
        assertContains(document, "/api/v1/departure-locations:")
        assertContains(document, "/api/v1/auth/guest-token:")
        assertContains(document, "List buses")
        assertContains(document, "A valid Bearer access token is required.")

        val swaggerResponse = client.get("/swagger")
        assertEquals(HttpStatusCode.OK, swaggerResponse.status)
    }

    @Test
    fun testBusFiltersUseDatabaseAndRequireCompleteTimeRange() = testApplication {
        application {
            module()
        }

        client.get("/")

        val busService = BusService()
        val testOrigin = "FilterTest${UUID.randomUUID()}"
        val testDestination = "Destination${UUID.randomUUID()}"
        val buses = listOf(
            Bus(
                id = UUID.randomUUID().toString(),
                departingFrom = testOrigin,
                destinationEnglish = testDestination,
                destinationTamil = "சோதனை",
                busName = "Filter Match",
                note = "Matching bus",
                departTime = "06:15 AM",
                days = listOf(Days.Monday)
            ),
            Bus(
                id = UUID.randomUUID().toString(),
                departingFrom = testOrigin,
                destinationEnglish = testDestination,
                destinationTamil = "சோதனை",
                busName = "Outside Time",
                note = "Outside time range",
                departTime = "07:15 PM",
                days = listOf(Days.Monday)
            ),
            Bus(
                id = UUID.randomUUID().toString(),
                departingFrom = testOrigin,
                destinationEnglish = "Other Destination",
                destinationTamil = "வேறு",
                busName = "Other Destination",
                note = "Destination mismatch",
                departTime = "07:00 AM",
                days = listOf(Days.Monday)
            ),
            Bus(
                id = UUID.randomUUID().toString(),
                departingFrom = "Other Origin",
                destinationEnglish = testDestination,
                destinationTamil = "சோதனை",
                busName = "Other Origin",
                note = "Origin mismatch",
                departTime = "08:00 AM",
                days = listOf(Days.Monday)
            )
        )

        buses.forEach { busService.addBus(it) }

        try {
            val response = client.get("/api/v1/buses") {
                parameter("departureLocation", testOrigin)
                parameter("destination", testDestination)
                parameter("departureTimeFrom", "05:00")
                parameter("departureTimeTo", "12:00")
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val payload = Json.decodeFromString<Response<List<Bus>>>(response.bodyAsText())
            assertEquals(listOf(buses.first().id), payload.data?.map { it.id })

            val originsResponse = client.get("/api/v1/departure-locations")
            val originsPayload = Json.decodeFromString<Response<List<String>>>(originsResponse.bodyAsText())
            assertTrue(testOrigin in originsPayload.data.orEmpty())

            val destinationsResponse = client.get("/api/v1/departure-locations/$testOrigin/destinations")
            val destinationsPayload = Json.decodeFromString<Response<List<String>>>(destinationsResponse.bodyAsText())
            assertTrue(testDestination in destinationsPayload.data.orEmpty())

            val invalidTimeRangeResponse = client.get("/api/v1/buses") {
                parameter("departureTimeFrom", "05:00")
            }
            assertEquals(HttpStatusCode.BadRequest, invalidTimeRangeResponse.status)

            val legacyResponse = client.get("/busService")
            assertEquals(HttpStatusCode.NotFound, legacyResponse.status)

            val missingBusDeleteResponse = client.delete("/api/v1/buses/missing-${UUID.randomUUID()}")
            assertEquals(HttpStatusCode.Unauthorized, missingBusDeleteResponse.status)
        } finally {
            buses.forEach { busService.deleteBusById(it.id) }
        }
    }

    @Test
    fun testAuthRefreshAndAdminOnlyBusMutations() = testApplication {
        application {
            module()
        }

        client.get("/")

        val userService = UserService()
        val busService = BusService()
        val username = "auth-${UUID.randomUUID()}"
        val email = "$username@example.test"
        val password = "test-password-123"
        val adminUsername = "admin-${UUID.randomUUID()}"
        val adminEmail = "$adminUsername@example.test"
        val adminPassword = "admin-test-password-123"
        val admin = userService.createUser(adminUsername, adminEmail, adminPassword, isAdmin = true)
        assertNotNull(admin)
        val bus = Bus(
            id = UUID.randomUUID().toString(),
            departingFrom = "Auth test origin",
            destinationEnglish = "Auth test destination",
            destinationTamil = "சோதனை",
            busName = "Auth test bus",
            note = "Auth integration test",
            departTime = "06:15 AM",
            days = listOf(Days.Monday)
        )
        var registeredUserId: String? = null

        try {
            val unauthenticatedWrite = client.post("/api/v1/buses") {
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(bus))
            }
            assertEquals(HttpStatusCode.Unauthorized, unauthenticatedWrite.status)

            val guestResponse = client.get("/api/v1/auth/guest-token")
            assertEquals(HttpStatusCode.OK, guestResponse.status)
            val guestToken = Json.decodeFromString<Response<GuestTokenResponse>>(guestResponse.bodyAsText()).data!!
            assertEquals(900L, guestToken.expiresIn)

            val guestWrite = client.post("/api/v1/buses") {
                header(HttpHeaders.Authorization, "Bearer ${guestToken.accessToken}")
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(bus))
            }
            assertEquals(HttpStatusCode.Forbidden, guestWrite.status)

            val registrationResponse = client.post("/api/v1/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(RegisterRequest(username, email, password)))
            }
            assertEquals(HttpStatusCode.Created, registrationResponse.status)
            val registeredTokens = Json.decodeFromString<Response<TokenResponse>>(registrationResponse.bodyAsText()).data!!
            registeredUserId = userService.getUserByUsername(username)?.id
            assertNotNull(registeredUserId)

            val userWrite = client.post("/api/v1/buses") {
                header(HttpHeaders.Authorization, "Bearer ${registeredTokens.accessToken}")
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(bus))
            }
            assertEquals(HttpStatusCode.Forbidden, userWrite.status)

            val loginResponse = client.post("/api/v1/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(LoginRequest(adminUsername, adminPassword)))
            }
            assertEquals(HttpStatusCode.OK, loginResponse.status)
            val adminTokens = Json.decodeFromString<Response<TokenResponse>>(loginResponse.bodyAsText()).data!!

            val refreshResponse = client.post("/api/v1/auth/refresh") {
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(RefreshTokenRequest(adminTokens.refreshToken)))
            }
            assertEquals(HttpStatusCode.OK, refreshResponse.status)
            val refreshedTokens = Json.decodeFromString<Response<TokenResponse>>(refreshResponse.bodyAsText()).data!!
            assertNotEquals(adminTokens.refreshToken, refreshedTokens.refreshToken)

            val replayedRefresh = client.post("/api/v1/auth/refresh") {
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(RefreshTokenRequest(adminTokens.refreshToken)))
            }
            assertEquals(HttpStatusCode.Unauthorized, replayedRefresh.status)

            val createResponse = client.post("/api/v1/buses") {
                header(HttpHeaders.Authorization, "Bearer ${refreshedTokens.accessToken}")
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(bus))
            }
            assertEquals(HttpStatusCode.Created, createResponse.status)
        } finally {
            busService.deleteBusById(bus.id)
            DatabaseFactory.dbQuery {
                RefreshTokens.deleteWhere { RefreshTokens.userId eq admin.id }
                Users.deleteWhere { Users.id eq admin.id }
                registeredUserId?.let { userId ->
                    RefreshTokens.deleteWhere { RefreshTokens.userId eq userId }
                    Users.deleteWhere { Users.id eq userId }
                }
            }
        }
    }

    @Test
    fun testYamlConfig() {
        val config = ApplicationConfig("application.yaml")
        val port = config.property("ktor.deployment.port").getString()
        assertEquals("8080", port)
    }

    @Test
    fun testServerStart() {
        val server = embeddedServer(
            Netty,
            port = 8089,
            host = "0.0.0.0",
            module = Application::module
        )
        server.start(wait = false)
        server.stop(1000, 1000)
    }

    @Test
    fun testEngineMain() {
        val thread = Thread {
            try {
                main()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
        thread.start()
        Thread.sleep(2000)
        thread.interrupt()
    }
}