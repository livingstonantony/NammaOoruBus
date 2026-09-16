package com.ell.nammaooru.services

import com.ell.nammaooru.data.Bus

class BusService {
    private val dummyBuses = mutableListOf(
        Bus( name="Route 1A", routeNumber = "1A"),
        Bus( name = "Route 2B", routeNumber =  "2B")
    )

    fun getBuses(): List<Bus> {
        return dummyBuses
    }

    fun getBusById(busId: String): Bus? {
        return dummyBuses.find { it.id == busId }
    }

    fun addBus(bus: Bus): Bus {
        val newBus = Bus(name =bus.name, routeNumber =  bus.routeNumber)
        dummyBuses.add(newBus)
        return newBus
    }

    fun updateBus(busId: String, bus: Bus): Bus? {
        val index = dummyBuses.indexOfFirst { it.id == busId }
        if (index != -1) {
            val updatedBus = bus.copy(id = busId)
            dummyBuses[index] = updatedBus
            return updatedBus
        }
        return null
    }

    fun deleteBusById(busId: String): Boolean {
        return dummyBuses.removeIf { it.id == busId }
    }
}
