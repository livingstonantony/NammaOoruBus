package com.ell.nammaooru

import com.ell.nammaooru.domain.model.Bus
import com.ell.nammaooru.domain.model.Days
import com.ell.nammaooru.ui.screens.busesForDay
import com.ell.nammaooru.ui.screens.filterBuses
import com.ell.nammaooru.ui.screens.upcomingBuses
import com.ell.nammaooru.ui.widgets.TimeFilter
import kotlin.test.Test
import kotlin.test.assertEquals

class SharedCommonTest {

    @Test
    fun example() {
        assertEquals(3, 1 + 2)
    }

    @Test
    fun busFiltersCombineOriginDestinationAndTimeWithoutCollapsingTrips() {
        val buses = listOf(
            testBus("morning", "Viriyur", "Sankarapuram", "05:45 AM"),
            testBus("evening", "Viriyur", "Kallakurichi", "05:45 PM"),
            testBus("other-origin", "Kallakurichi", "Sankarapuram", "06:15 AM")
        )

        val filtered = filterBuses(
            buses = buses,
            departureLocation = "Viriyur",
            destination = "Sankarapuram",
            timeFilter = TimeFilter.MORNING
        )

        assertEquals(listOf("morning"), filtered.map { it.id })
    }

    @Test
    fun allDayFilterKeepsAllTripsFromSelectedOrigin() {
        val buses = listOf(
            testBus("early", "Viriyur", "Sankarapuram", "05:45 AM"),
            testBus("late", "Viriyur", "Kallakurichi", "05:45 PM"),
            testBus("other-origin", "Kallakurichi", "Sankarapuram", "06:15 AM")
        )

        val filtered = filterBuses(
            buses = buses,
            departureLocation = "Viriyur",
            destination = null,
            timeFilter = TimeFilter.ALL_DAY
        )

        assertEquals(listOf("early", "late"), filtered.map { it.id })
    }

    @Test
    fun upcomingListOnlyContainsRemainingDeparturesForToday() {
        val buses = listOf(
            testBus("past", "Viriyur", "Sankarapuram", "08:00 AM").copy(days = Days.entries),
            testBus("weekday", "Viriyur", "Kallakurichi", "09:30 AM").copy(
                days = listOf(Days.Monday, Days.Tuesday, Days.Wednesday, Days.Thursday, Days.Friday)
            ),
            testBus("weekend", "Viriyur", "Salem", "10:00 AM").copy(
                days = listOf(Days.Saturday, Days.Sunday)
            ),
            testBus("later", "Viriyur", "Salem", "11:00 AM").copy(days = Days.entries)
        )

        val today = busesForDay(buses, Days.Wednesday)
        val upcoming = upcomingBuses(today, currentMinutes = 9 * 60)

        assertEquals(listOf("weekday", "later"), upcoming.map { it.id })
    }

    private fun testBus(
        id: String,
        departingFrom: String,
        destination: String,
        departTime: String
    ) = Bus(
        id = id,
        departingFrom = departingFrom,
        destinationEnglish = destination,
        destinationTamil = "",
        busName = "Test route",
        note = "",
        departTime = departTime,
        days = listOf(Days.Monday)
    )
}