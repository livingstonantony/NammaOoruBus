package com.ell.nammaooru.services

import kotlinx.serialization.Serializable
import java.util.UUID


data class Stop(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val time: String,
)

@Serializable
data class Bus(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val stop: String,
    )


val dummyBuses = mutableListOf(
    Bus(name = "Bus 1", stop = "Stop 1"),
    Bus(name = "Bus 2", stop = "Stop 2"),
    Bus(name = "Bus 3", stop = "Stop 3"),
)

class BusService {

    fun getBus(id: String): Bus? {
        return dummyBuses.find { it.id == id }
    }
    fun getBuses(): List<Bus> {
        return dummyBuses
    }
    fun addBus(bus: Bus): Bus {
        dummyBuses.add(bus)
        return bus
    }
    fun updateBus(id: String, bus: Bus): Bus? {
        val index = dummyBuses.indexOfFirst { it.id == id }
        if (index != -1) {
            dummyBuses[index] = bus.copy(id = id)
            return bus
        }
        return null
    }


    fun deleteBus(id: String): Boolean {
        val index = dummyBuses.indexOfFirst { it.id == id }
        if (index != -1) {
            dummyBuses.removeAt(index)
            return true
        }
        return false
    }

}