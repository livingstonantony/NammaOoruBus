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

fun Route.busRoutes(busService: BusService) {

    route("/busService") {


        // GET /busService
        get {

            val data: List<Bus> = busService.getBuses()

            val buses = Response(
                result = "success",
                message = "Bus services fetched successfully",
                data = data
            )

            call.respond(buses)
        }


        // GET /busService/1
        get("/{id}") {

            val id = call.parameters["id"]

            if (id == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    Response(
                        result = "failed",
                        message = "Invalid bus id",
                        data = null
                    )
                )
                return@get
            }

            val bus: Bus? = busService.getBusById(id)

            if (bus == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    Response(
                        result = "failed",
                        message = "Bus not found",
                        data = null
                    )
                )
            } else {

                call.respond(
                    Response(
                        result = "success",
                        message = "Bus fetched successfully",
                        data = bus
                    )
                )
            }
        }


        // POST /busService
        post("") {

            try {

                val bus = call.receive<Bus>()

                busService.addBus(bus)

                call.respond(
                    HttpStatusCode.Created,
                    Response(
                        result = "success",
                        message = "Bus created successfully",
                        data = bus
                    )
                )

            } catch (e: Exception) {

                call.respond(
                    HttpStatusCode.BadRequest,
                    Response(
                        result = "failed",
                        message = "Invalid bus data: $e",
                        data = null
                    )
                )
            }
        }


        // PUT /busService/1
        put("/{id}") {

            val id = call.parameters["id"]

            if (id == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    Response(
                        result = "failed",
                        message = "Invalid bus id",
                        data = null
                    )
                )
                return@put
            }

            try {

                val bus = call.receive<Bus>()

                val updatedBus = busService.updateBus(
                    id,
                    bus
                )

                if (updatedBus == null) {

                    call.respond(
                        HttpStatusCode.NotFound,
                        Response(
                            result = "failed",
                            message = "Bus not found",
                            data = null
                        )
                    )

                } else {

                    call.respond(
                        HttpStatusCode.OK,
                        updatedBus
                    )
                }

            } catch (e: Exception) {

                call.respond(
                    HttpStatusCode.BadRequest,
                    Response(
                        result = "failed",
                        message = "Invalid bus data: $e",
                        data = null
                    )
                )
            }
        }


        // DELETE /busService/1
        delete("/{id}") {

            val id = call.parameters["id"]

            if (id == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    Response(
                        result = "failed",
                        message = "Invalid bus id",
                        data = null
                    )
                )
                return@delete
            }

            busService.deleteBusById(id)

            call.respond(
                HttpStatusCode.OK,
                Response(
                    result = "success",
                    message = "Bus deleted successfully",
                    data = null
                )
            )

        }
    }
}