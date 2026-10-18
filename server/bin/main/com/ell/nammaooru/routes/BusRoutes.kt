package com.ell.nammaooru.routes

import com.ell.nammaooru.data.Bus
import com.ell.nammaooru.data.Response
import com.ell.nammaooru.services.BusService
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.openapi.jsonSchema
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.openapi.describe
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.utils.io.ExperimentalKtorApi
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

@OptIn(ExperimentalKtorApi::class)
fun Route.busRoutes(busService: BusService) {
    busCollectionRoutes(busService)

    route("/api/v1/departure-locations") {
        get {
            call.respond(
                Response(
                    result = "success",
                    message = "Departure locations fetched successfully",
                    data = busService.getAllDepartingFrom()
                )
            )
        }.describe {
            summary = "List departure locations"
            tag("Departure locations")
            responses {
                HttpStatusCode.OK {
                    description = "Departure locations read from the timetable."
                    ContentType.Application.Json {
                        schema = jsonSchema<Response<List<String>>>()
                    }
                }
            }
        }

        get("/{departureLocation}/destinations") {
            val departureLocation = call.parameters["departureLocation"]
            if (departureLocation == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    Response(
                        result = "failed",
                        message = "Departure location is required",
                        data = null
                    )
                )
                return@get
            }

            call.respond(
                Response(
                    result = "success",
                    message = "Destinations fetched successfully",
                    data = busService.getBusDestinationEnglishByDepartingFrom(departureLocation)
                )
            )
        }.describe {
            summary = "List destinations for a departure location"
            tag("Departure locations")
            parameters {
                path("departureLocation") {
                    description = "Origin bus stop."
                    schema = jsonSchema<String>()
                }
            }
            responses {
                HttpStatusCode.OK {
                    description = "English destinations available from this location."
                    ContentType.Application.Json {
                        schema = jsonSchema<Response<List<String>>>()
                    }
                }
                HttpStatusCode.BadRequest {
                    description = "The departure location is required."
                }
            }
        }
    }
}

@OptIn(ExperimentalKtorApi::class)
private fun Route.busCollectionRoutes(busService: BusService) {
    route("/api/v1/buses") {
        get {
            val queryParameters = call.request.queryParameters
            val timeFromParameter = queryParameters["departureTimeFrom"] ?: queryParameters["timeFrom"]
            val timeToParameter = queryParameters["departureTimeTo"] ?: queryParameters["timeTo"]
            val timeFrom = timeFromParameter?.let(::parseQueryTime)
            val timeTo = timeToParameter?.let(::parseQueryTime)

            if ((timeFromParameter == null) != (timeToParameter == null) ||
                (timeFromParameter != null && (timeFrom == null || timeTo == null))
            ) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    Response(
                        result = "failed",
                        message = "departureTimeFrom and departureTimeTo must both use 24-hour HH:mm format",
                        data = null
                    )
                )
                return@get
            }

            call.respond(
                Response(
                    result = "success",
                    message = "Bus services fetched successfully",
                    data = busService.getBuses(
                        departingFrom = queryParameters["departureLocation"]
                            ?: queryParameters["departingFrom"],
                        destinationEnglish = queryParameters["destination"]
                            ?: queryParameters["destinationEnglish"],
                        timeFrom = timeFrom,
                        timeTo = timeTo
                    )
                )
            )
        }.describe {
            summary = "List buses"
            description = "Filters by departure location, English destination, and optional inclusive departure-time range."
            tag("Buses")
            parameters {
                query("departureLocation") {
                    description = "Filter by origin bus stop."
                    schema = jsonSchema<String>()
                }
                query("destination") {
                    description = "Filter by English destination name."
                    schema = jsonSchema<String>()
                }
                query("departureTimeFrom") {
                    description = "Inclusive lower time bound in 24-hour HH:mm format; provide with departureTimeTo."
                    schema = jsonSchema<String>()
                }
                query("departureTimeTo") {
                    description = "Inclusive upper time bound in 24-hour HH:mm format; provide with departureTimeFrom."
                    schema = jsonSchema<String>()
                }
            }
            responses {
                HttpStatusCode.OK {
                    description = "Matching buses ordered by departure time."
                    ContentType.Application.Json {
                        schema = jsonSchema<Response<List<Bus>>>()
                    }
                }
                HttpStatusCode.BadRequest {
                    description = "The departure-time range is incomplete or invalid."
                }
            }
        }

        get("/{busId}") {
            val busId = call.parameters["busId"]
            if (busId == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    Response(result = "failed", message = "Bus ID is required", data = null)
                )
                return@get
            }

            val bus = busService.getBusById(busId)
            if (bus == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    Response(result = "failed", message = "Bus not found", data = null)
                )
            } else {
                call.respond(
                    Response(result = "success", message = "Bus fetched successfully", data = bus)
                )
            }
        }.describe {
            summary = "Get a bus"
            tag("Buses")
            parameters {
                path("busId") {
                    description = "Bus identifier."
                    schema = jsonSchema<String>()
                }
            }
            responses {
                HttpStatusCode.OK {
                    description = "Bus found."
                    ContentType.Application.Json {
                        schema = jsonSchema<Response<Bus>>()
                    }
                }
                HttpStatusCode.NotFound {
                    description = "No bus exists with this identifier."
                }
            }
        }

        authenticate("auth-jwt") {
        post {
            val isAdmin = call.principal<JWTPrincipal>()?.payload?.getClaim("isAdmin")?.asBoolean() == true
            if (!isAdmin) {
                call.respond(
                    HttpStatusCode.Forbidden,
                    Response(
                        result = "failed",
                        message = "Only admins can create buses",
                        data = null
                    )
                )
                return@post
            }
            try {
                val bus = call.receive<Bus>()
                val createdBus = busService.addBus(bus)
                call.respond(
                    HttpStatusCode.Created,
                    Response(result = "success", message = "Bus created successfully", data = createdBus)
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    Response(result = "failed", message = "Invalid bus data: $e", data = null)
                )
            }
        }.describe {
            summary = "Create a bus"
            description = "Requires an admin Bearer access token."
            tag("Buses")
            requestBody {
                required = true
                description = "Bus timetable record to create."
                ContentType.Application.Json {
                    schema = jsonSchema<Bus>()
                }
            }
            responses {
                HttpStatusCode.Created {
                    description = "Bus created."
                    ContentType.Application.Json {
                        schema = jsonSchema<Response<Bus>>()
                    }
                }
                HttpStatusCode.BadRequest {
                    description = "The bus request body is invalid."
                }
                HttpStatusCode.Forbidden {
                    description = "Only admins can create buses."
                }
                HttpStatusCode.Unauthorized {
                    description = "A valid Bearer access token is required."
                }
            }
        }

        put("/{busId}") {
            val isAdmin = call.principal<JWTPrincipal>()?.payload?.getClaim("isAdmin")?.asBoolean() == true
            if (!isAdmin) {
                call.respond(
                    HttpStatusCode.Forbidden,
                    Response(
                        result = "failed",
                        message = "Only admins can update buses",
                        data = null
                    )
                )
                return@put
            }
            val busId = call.parameters["busId"]
            if (busId == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    Response(result = "failed", message = "Bus ID is required", data = null)
                )
                return@put
            }

            try {
                val bus = call.receive<Bus>()
                val updatedBus = busService.updateBus(busId, bus)
                if (updatedBus == null) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        Response(result = "failed", message = "Bus not found", data = null)
                    )
                } else {
                    call.respond(
                        Response(result = "success", message = "Bus updated successfully", data = updatedBus)
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    Response(result = "failed", message = "Invalid bus data: $e", data = null)
                )
            }
        }.describe {
            summary = "Replace a bus"
            description = "Requires an admin Bearer access token."
            tag("Buses")
            parameters {
                path("busId") {
                    description = "Bus identifier."
                    schema = jsonSchema<String>()
                }
            }
            requestBody {
                required = true
                description = "Replacement bus timetable record."
                ContentType.Application.Json {
                    schema = jsonSchema<Bus>()
                }
            }
            responses {
                HttpStatusCode.OK {
                    description = "Bus updated."
                    ContentType.Application.Json {
                        schema = jsonSchema<Response<Bus>>()
                    }
                }
                HttpStatusCode.BadRequest {
                    description = "The bus request body is invalid."
                }
                HttpStatusCode.NotFound {
                    description = "No bus exists with this identifier."
                }
                HttpStatusCode.Forbidden {
                    description = "Only admins can update buses."
                }
                HttpStatusCode.Unauthorized {
                    description = "A valid Bearer access token is required."
                }
            }
        }

        delete("/{busId}") {
            val isAdmin = call.principal<JWTPrincipal>()?.payload?.getClaim("isAdmin")?.asBoolean() == true
            if (!isAdmin) {
                call.respond(
                    HttpStatusCode.Forbidden,
                    Response(
                        result = "failed",
                        message = "Only admins can delete buses",
                        data = null
                    )
                )
                return@delete
            }
            val busId = call.parameters["busId"]
            if (busId == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    Response(result = "failed", message = "Bus ID is required", data = null)
                )
                return@delete
            }

            if (!busService.deleteBusById(busId)) {
                call.respond(
                    HttpStatusCode.NotFound,
                    Response(result = "failed", message = "Bus not found", data = null)
                )
                return@delete
            }

            call.respond(
                Response(result = "success", message = "Bus deleted successfully", data = null)
            )
        }.describe {
            summary = "Delete a bus"
            description = "Requires an admin Bearer access token."
            tag("Buses")
            parameters {
                path("busId") {
                    description = "Bus identifier."
                    schema = jsonSchema<String>()
                }
            }
            responses {
                HttpStatusCode.OK {
                    description = "Bus deleted."
                }
                HttpStatusCode.NotFound {
                    description = "No bus exists with this identifier."
                }
                HttpStatusCode.Forbidden {
                    description = "Only admins can delete buses."
                }
                HttpStatusCode.Unauthorized {
                    description = "A valid Bearer access token is required."
                }
            }
        }
        }
    }
}

private val queryTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    .withResolverStyle(ResolverStyle.STRICT)

private fun parseQueryTime(value: String): LocalTime? =
    runCatching { LocalTime.parse(value, queryTimeFormatter) }.getOrNull()