package com.ell.nammaooru.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ell.nammaooru.domain.model.Bus
import com.ell.nammaooru.domain.model.Days
import com.ell.nammaooru.feature.bus.BusIntent
import com.ell.nammaooru.feature.bus.BusViewModel
import com.ell.nammaooru.feature.bus.UiState
import com.ell.nammaooru.ui.widgets.BusCard
import com.ell.nammaooru.ui.widgets.DepartingFromCard
import com.ell.nammaooru.ui.widgets.FilterBus
import com.ell.nammaooru.ui.widgets.TimeFilter
import kotlinx.coroutines.delay
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BusesScreen(
    showFullTimetable: Boolean,
    selectedDepartureLocation: String?,
    onDepartureLocationSelected: (String?) -> Unit,
    selectedDestination: String?,
    onDestinationSelected: (String?) -> Unit,
    selectedTimeFilter: TimeFilter,
    onTimeFilterSelected: (TimeFilter) -> Unit,
    showTamilNames: Boolean,
    onOpenFullTimetable: () -> Unit,
    modifier: Modifier = Modifier,
    isAdmin: Boolean = false,
    onEditBus: (Bus) -> Unit = {},
    onDeleteBus: (Bus) -> Unit = {},
    viewModel: BusViewModel = koinViewModel()
) {
    val uiState by viewModel.stateBuses.collectAsState()
    var currentLocalDateTime by remember { mutableStateOf(readLocalDateTime()) }

    LaunchedEffect(Unit) {
        viewModel.processIntent(BusIntent.GetBuses)
        while (true) {
            delay(30_000)
            currentLocalDateTime = readLocalDateTime()
        }
    }

    when (val state = uiState) {
        is UiState.Loading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is UiState.Success -> {
            val origins = state.data.map { it.departingFrom }.distinct().sorted()
            val activeOrigin = selectedDepartureLocation?.takeIf { it in origins }
            val destinations = state.data
                .filter { activeOrigin == null || it.departingFrom == activeOrigin }
                .map { it.destinationEnglish }
                .filter { it.isNotBlank() }
                .distinct()
                .sorted()

            LaunchedEffect(origins, selectedDepartureLocation) {
                if (selectedDepartureLocation != null && selectedDepartureLocation !in origins) {
                    onDepartureLocationSelected(origins.firstOrNull())
                }
            }
            LaunchedEffect(destinations, selectedDestination) {
                if (selectedDestination != null && selectedDestination !in destinations) {
                    onDestinationSelected(null)
                }
            }

            val filteredBuses = filterBuses(
                buses = state.data,
                departureLocation = activeOrigin,
                destination = selectedDestination?.takeIf { it in destinations },
                timeFilter = selectedTimeFilter
            )
            val todayBuses = if (isAdmin) {
                filteredBuses
            } else {
                busesForDay(filteredBuses, currentLocalDateTime.dayOfWeek.toBusDay())
            }
            val visibleBuses = if (showFullTimetable || isAdmin) {
                todayBuses
            } else {
                upcomingBuses(
                    todayBuses,
                    currentMinutes = currentLocalDateTime.hour * 60 + currentLocalDateTime.minute
                )
            }

            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 12.dp,
                    end = 16.dp,
                    bottom = 20.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    TimetableHeader(
                        showFullTimetable = showFullTimetable || isAdmin,
                        onRefresh = { viewModel.processIntent(BusIntent.GetBuses) }
                    )
                }
                item {
                    DepartingFromCard(
                        origins = origins,
                        selectedOrigin = activeOrigin,
                        destinations = destinations,
                        selectedDestination = selectedDestination?.takeIf { it in destinations },
                        onOriginSelected = onDepartureLocationSelected,
                        onDestinationSelected = onDestinationSelected,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    FilterBus(
                        title = "Departure time",
                        tripCountText = "${filteredBuses.size} trips",
                        selectedFilter = selectedTimeFilter,
                        onFilterSelected = onTimeFilterSelected
                    )
                }
                item {
                    Column {
                        Text(
                            text = if (showFullTimetable) "Full daily timetable" else "Leaving soon",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!showFullTimetable && todayBuses.size > visibleBuses.size) {
                            Text(
                                text = "Showing the next ${visibleBuses.size} of ${todayBuses.size} departures today",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                if (visibleBuses.isEmpty()) {
                    item {
                        EmptyDepartures(onReset = {
                            onDestinationSelected(null)
                            onTimeFilterSelected(TimeFilter.ALL_DAY)
                        })
                    }
                } else {
                    items(items = visibleBuses, key = { it.id }) { bus ->
                        BusCard(
                            bus = bus,
                            showTamilName = showTamilNames,
                            showDepartureLocation = activeOrigin == null,
                            onEdit = if (isAdmin) ({ onEditBus(bus) }) else null,
                            onDelete = if (isAdmin) ({ onDeleteBus(bus) }) else null
                        )
                    }
                    if (!showFullTimetable && todayBuses.size > visibleBuses.size) {
                        item {
                            Button(
                                onClick = onOpenFullTimetable,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("View full timetable")
                            }
                        }
                    }
                }
            }
        }

        is UiState.Error -> {
            Column(
                modifier = modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Timetable unavailable", style = MaterialTheme.typography.titleMedium)
                Text(state.message, modifier = Modifier.padding(top = 8.dp, bottom = 16.dp))
                Button(onClick = { viewModel.processIntent(BusIntent.GetBuses) }) {
                    Text("Try again")
                }
            }
        }
    }
}

private fun readLocalDateTime(): LocalDateTime =
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

private fun DayOfWeek.toBusDay(): Days = when (this) {
    DayOfWeek.MONDAY -> Days.Monday
    DayOfWeek.TUESDAY -> Days.Tuesday
    DayOfWeek.WEDNESDAY -> Days.Wednesday
    DayOfWeek.THURSDAY -> Days.Thursday
    DayOfWeek.FRIDAY -> Days.Friday
    DayOfWeek.SATURDAY -> Days.Saturday
    DayOfWeek.SUNDAY -> Days.Sunday
}

@Composable
private fun TimetableHeader(showFullTimetable: Boolean, onRefresh: () -> Unit) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Viriyur Village Bus Timings",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (showFullTimetable) "Every scheduled departure" else "Upcoming local departures",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onRefresh) {
            Icon(Icons.Outlined.Refresh, contentDescription = "Refresh timetable")
        }
    }
}

@Composable
private fun EmptyDepartures(onReset: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("No buses match these filters", style = MaterialTheme.typography.titleMedium)
        Text(
            "Try another destination or time of day.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(onClick = onReset) { Text("Clear destination and time") }
    }
}

val sampleBuses = listOf(
    Bus(
        id = "1",
        departingFrom = "Kallakurichi",
        destinationEnglish = "Sankarapuram",
        destinationTamil = "சங்கராபுரம்",
        busName = "TNSTC 12A",
        note = "First Morning Bus | Direct Route",
        departTime = "05:45 AM",
        days = Days.entries
    ),
    Bus(
        id = "2",
        departingFrom = "Kallakurichi",
        destinationEnglish = "Tiruvannamalai",
        destinationTamil = "திருவண்ணாமலை",
        busName = "TNSTC 105",
        note = "Express Bus | Via Sankarapuram",
        departTime = "06:15 AM",
        days = Days.entries
    ),
    Bus(
        id = "3",
        departingFrom = "Kallakurichi",
        destinationEnglish = "Salem",
        destinationTamil = "சேலம்",
        busName = "SETC 302",
        note = "Super Fast Express",
        departTime = "07:00 AM",
        days = listOf(Days.Monday, Days.Tuesday, Days.Wednesday, Days.Thursday, Days.Friday)
    )
)

