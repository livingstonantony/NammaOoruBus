package com.ell.nammaooru.services

import com.ell.nammaooru.data.Bus
import com.ell.nammaooru.database.Buses
import com.ell.nammaooru.database.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class BusService {

    suspend fun getBuses(): List<Bus> = dbQuery {
        Buses.selectAll().map { it.toBus() }
    }

    suspend fun getBusById(busId: String): Bus? = dbQuery {
        Buses.selectAll()
            .where { Buses.id eq busId }
            .map { it.toBus() }
            .singleOrNull()
    }

    suspend fun addBus(bus: Bus): Bus = dbQuery {
        Buses.insert {
            it[id] = bus.id
            it[destinationEnglish] = bus.destinationEnglish
            it[destinationTamil] = bus.destinationTamil
            it[busName] = bus.busName
            it[note] = bus.note
            it[departTime] = bus.departTime
        }
        bus
    }

    suspend fun updateBus(busId: String, bus: Bus): Bus? = dbQuery {
        val updatedRows = Buses.update({ Buses.id eq busId }) {
            it[destinationEnglish] = bus.destinationEnglish
            it[destinationTamil] = bus.destinationTamil
            it[busName] = bus.busName
            it[note] = bus.note
            it[departTime] = bus.departTime
        }

        if (updatedRows == 0) {
            return@dbQuery null
        }

        getBusById(busId)
    }

    suspend fun deleteBusById(busId: String): Boolean = dbQuery {
        Buses.deleteWhere { id eq busId } > 0
    }

    private fun ResultRow.toBus(): Bus = Bus(
        id = this[Buses.id],
        destinationEnglish = this[Buses.destinationEnglish],
        destinationTamil = this[Buses.destinationTamil],
        busName = this[Buses.busName],
        note = this[Buses.note],
        departTime = this[Buses.departTime],
    )
}
