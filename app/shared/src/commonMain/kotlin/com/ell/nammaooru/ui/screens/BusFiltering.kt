package com.ell.nammaooru.ui.screens

import com.ell.nammaooru.domain.model.Bus
import com.ell.nammaooru.domain.model.Days
import com.ell.nammaooru.ui.widgets.TimeFilter

internal fun filterBuses(
    buses: List<Bus>,
    departureLocation: String?,
    destination: String?,
    timeFilter: TimeFilter
): List<Bus> = buses
    .filter { bus ->
        (departureLocation == null || bus.departingFrom == departureLocation) &&
            (destination == null || bus.destinationEnglish == destination) &&
            timeFilter.includes(bus.departTime)
    }
    .sortedBy { parseDepartureMinutes(it.departTime) ?: Int.MAX_VALUE }

internal fun TimeFilter.includes(departureTime: String): Boolean {
    if (this == TimeFilter.ALL_DAY) return true

    val minutes = parseDepartureMinutes(departureTime) ?: return false
    return minutes >= startMinute && minutes < endMinuteExclusive
}

internal fun busesForDay(buses: List<Bus>, day: Days): List<Bus> =
    buses.filter { it.days.isEmpty() || day in it.days }

internal fun upcomingBuses(
    buses: List<Bus>,
    currentMinutes: Int,
    limit: Int = 5
): List<Bus> = buses
    .filter { bus ->
        val departureMinutes = parseDepartureMinutes(bus.departTime)
        departureMinutes != null && departureMinutes >= currentMinutes
    }
    .take(limit)

internal fun parseDepartureMinutes(departureTime: String): Int? {
    val normalized = departureTime.trim().uppercase()
    val meridiem = when {
        normalized.endsWith("AM") -> "AM"
        normalized.endsWith("PM") -> "PM"
        else -> null
    }
    val time = if (meridiem == null) normalized else normalized.dropLast(2).trim()
    val parts = time.split(":")
    if (parts.size != 2) return null

    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null
    if (minute !in 0..59) return null

    val hourOfDay = when (meridiem) {
        "AM" -> {
            if (hour !in 1..12) return null
            hour % 12
        }
        "PM" -> {
            if (hour !in 1..12) return null
            hour % 12 + 12
        }
        else -> {
            if (hour !in 0..23) return null
            hour
        }
    }

    return hourOfDay * 60 + minute
}