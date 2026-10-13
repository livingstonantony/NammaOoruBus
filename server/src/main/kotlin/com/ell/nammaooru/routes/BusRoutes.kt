package com.ell.nammaooru.routes

import com.ell.nammaooru.data.Bus
import com.ell.nammaooru.data.Response
import com.ell.nammaooru.services.BusService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

fun Route.busRoutes(busService: BusService) {
    busCollectionRoutes(busService, "/api/v1/buses", "busId")
    busCollectionRoutes(busService, "/busService", "id")

    route("/api/v1/departure-locations") {
        get {
            call.respond(
                Response(
                    result = "success",
                    message = "Departure locations fetched successfully",
                    data = busService.getAllDepartingFrom()
                )
            )
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
        }
    }

    route("/busService") {
        get("/departingFrom") {
            call.respond(
                Response(
                    result = "success",
                    message = "Bus departingFrom fetched successfully",
                    data = busService.getAllDepartingFrom()
                )
            )
        }

        get("/departingFromDestinations/{departingFrom}") {
            val departingFrom = call.parameters["departingFrom"]
            if (departingFrom == null) {
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
                    data = busService.getBusDestinationEnglishByDepartingFrom(departingFrom)
                )
            )
        }
    }
}

private fun Route.busCollectionRoutes(
    busService: BusService,
    path: String,
    busIdParameter: String
) {
    route(path) {
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
        }

        get("/{$busIdParameter}") {
            val busId = call.parameters[busIdParameter]
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
        }

        post {
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
        }

        put("/{$busIdParameter}") {
            val busId = call.parameters[busIdParameter]
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
        }

        delete("/{$busIdParameter}") {
            val busId = call.parameters[busIdParameter]
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
        }
    }
}

private val queryTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    .withResolverStyle(ResolverStyle.STRICT)

private fun parseQueryTime(value: String): LocalTime? =
    runCatching { LocalTime.parse(value, queryTimeFormatter) }.getOrNull()