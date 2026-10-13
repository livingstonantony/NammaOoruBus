package com.ell.nammaooru.services

import com.ell.nammaooru.data.Bus
import com.ell.nammaooru.database.Buses
import com.ell.nammaooru.database.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class BusService {

    suspend fun getBuses(
        departingFrom: String? = null,
        destinationEnglish: String? = null,
        timeFrom: LocalTime? = null,
        timeTo: LocalTime? = null
    ): List<Bus> = dbQuery {
        var query = Buses.selectAll()

        departingFrom?.takeIf { it.isNotBlank() }?.let { origin ->
            query = query.andWhere { Buses.departingFrom eq origin }
        }
        destinationEnglish?.takeIf { it.isNotBlank() }?.let { destination ->
            query = query.andWhere { Buses.destinationEnglish eq destination }
        }
        if (timeFrom != null && timeTo != null) {
            query = if (timeFrom <= timeTo) {
                query.andWhere {
                    (Buses.departTime greaterEq timeFrom) and (Buses.departTime lessEq timeTo)
                }
            } else {
                query.andWhere {
                    (Buses.departTime greaterEq timeFrom) or (Buses.departTime lessEq timeTo)
                }
            }
        }

        query.orderBy(Buses.departTime to SortOrder.ASC).map { it.toBus() }
    }

    suspend fun getBusById(busId: String): Bus? = dbQuery {
        Buses.selectAll()
            .where { Buses.id eq busId }
            .map { it.toBus() }
            .singleOrNull()
    }
    suspend fun getBusDestinationEnglishByDepartingFrom(departingFrom: String): List<String> = dbQuery {
        Buses.select(Buses.destinationEnglish)
            .where { Buses.departingFrom eq departingFrom }
            .map { it[Buses.destinationEnglish] }
            .distinct()
    }

    suspend fun getAllDepartingFrom(): List<String> = dbQuery {
        Buses.select(Buses.departingFrom)
            .map { it[Buses.departingFrom] }
            .distinct()
    }

    suspend fun addBus(bus: Bus): Bus = dbQuery {
        Buses.insert {
            it[id] = bus.id
            it[departingFrom] = bus.departingFrom
            it[destinationEnglish] = bus.destinationEnglish
            it[destinationTamil] = bus.destinationTamil
            it[busName] = bus.busName
            it[note] = bus.note
            it[departTime] = parseLocalTime(bus.departTime)
            it[days] = bus.days
        }
        bus
    }

    suspend fun updateBus(busId: String, bus: Bus): Bus? = dbQuery {
        val updatedRows = Buses.update({ Buses.id eq busId }) {
            it[departingFrom] = bus.departingFrom
            it[destinationEnglish] = bus.destinationEnglish
            it[destinationTamil] = bus.destinationTamil
            it[busName] = bus.busName
            it[note] = bus.note
            it[departTime] = parseLocalTime(bus.departTime)
            it[days] = bus.days
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
        departingFrom = this[Buses.departingFrom],
        destinationEnglish = this[Buses.destinationEnglish],
        destinationTamil = this[Buses.destinationTamil],
        busName = this[Buses.busName],
        note = this[Buses.note],
        departTime = this[Buses.departTime].toTimeFormat(),
        days = this[Buses.days]
    )
}

private val displayFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)

private fun parseLocalTime(timeStr: String): LocalTime {
    val trimmed = timeStr.trim()
    return try {
        LocalTime.parse(trimmed)
    } catch (_: Exception) {
        try {
            LocalTime.parse(trimmed, DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH))
        } catch (_: Exception) {
            try {
                LocalTime.parse(trimmed, DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH))
            } catch (_: Exception) {
                LocalTime.parse(trimmed, DateTimeFormatter.ofPattern("HH:mm"))
            }
        }
    }
}

fun LocalTime.toTimeFormat(): String {
    return this.format(displayFormatter)
}
