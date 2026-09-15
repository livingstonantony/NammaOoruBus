package com.ell.nammaooru.routes

import com.ell.nammaooru.data.Response
import com.ell.nammaooru.services.BusService
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.busRoutes(busService: BusService) {

    route("/busService") {

        get {
            val data = Response(
                "success",
                "Bus services fetched successfully",
                busService.getBuses()
            )
            call.respond(HttpStatusCode.OK, data)
        }

        get("/{busId}") {

        }

        post {

        }

        put("/{busId}") {

        }

        delete("/{busId}") {


        }

    }
}