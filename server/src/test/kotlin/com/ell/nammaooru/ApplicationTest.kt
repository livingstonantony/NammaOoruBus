package com.ell.nammaooru

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.Application
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.testing.*
import kotlin.test.*

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