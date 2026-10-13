package com.ell.nammaooru

import com.ell.nammaooru.data.Bus
import com.ell.nammaooru.data.Days
import com.ell.nammaooru.data.Response
import com.ell.nammaooru.services.BusService
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.Application
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.testing.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.test.*
import java.util.UUID

class ApplicationTest {

    @Test
    fun testRoot() = testApplication {
        application {
            module()
        }
        val response = client.get("/") {
            header("X-API-Key", "my-secret-nammaooru-key")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Hello from Ktor", response.bodyAsText())
    }

    @Test
    fun testOpenApiDocumentAndSwaggerUiAreAvailableWithoutApiKey() = testApplication {
        application {
            module()
        }

        val documentResponse = client.get("/openapi/documentation.yaml")
        assertEquals(HttpStatusCode.OK, documentResponse.status)
        assertContains(documentResponse.bodyAsText(), "openapi: 3.0.3")

        val swaggerResponse = client.get("/swagger")
        assertEquals(HttpStatusCode.OK, swaggerResponse.status)
    }

    @Test
    fun testBusFiltersUseDatabaseAndRequireCompleteTimeRange() = testApplication {
        application {
            module()
        }

        client.get("/") {
            header("X-API-Key", "my-secret-nammaooru-key")
        }

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
                header("X-API-Key", "my-secret-nammaooru-key")
                parameter("departureLocation", testOrigin)
                parameter("destination", testDestination)
                parameter("departureTimeFrom", "05:00")
                parameter("departureTimeTo", "12:00")
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val payload = Json.decodeFromString<Response<List<Bus>>>(response.bodyAsText())
            assertEquals(listOf(buses.first().id), payload.data?.map { it.id })

            val originsResponse = client.get("/api/v1/departure-locations") {
                header("X-API-Key", "my-secret-nammaooru-key")
            }
            val originsPayload = Json.decodeFromString<Response<List<String>>>(originsResponse.bodyAsText())
            assertTrue(testOrigin in originsPayload.data.orEmpty())

            val destinationsResponse = client.get("/api/v1/departure-locations/$testOrigin/destinations") {
                header("X-API-Key", "my-secret-nammaooru-key")
            }
            val destinationsPayload = Json.decodeFromString<Response<List<String>>>(destinationsResponse.bodyAsText())
            assertTrue(testDestination in destinationsPayload.data.orEmpty())

            val invalidTimeRangeResponse = client.get("/api/v1/buses") {
                header("X-API-Key", "my-secret-nammaooru-key")
                parameter("departureTimeFrom", "05:00")
            }
            assertEquals(HttpStatusCode.BadRequest, invalidTimeRangeResponse.status)

            val legacyResponse = client.get("/busService") {
                header("X-API-Key", "my-secret-nammaooru-key")
                parameter("departingFrom", testOrigin)
            }
            assertEquals(HttpStatusCode.OK, legacyResponse.status)

            val missingBusDeleteResponse = client.delete("/api/v1/buses/missing-${UUID.randomUUID()}") {
                header("X-API-Key", "my-secret-nammaooru-key")
            }
            assertEquals(HttpStatusCode.NotFound, missingBusDeleteResponse.status)
        } finally {
            buses.forEach { busService.deleteBusById(it.id) }
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
                main(emptyArray())
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
        thread.start()
        Thread.sleep(2000)
        thread.interrupt()
    }
}