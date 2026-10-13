package com.ell.nammaooru

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.ell.nammaooru.ui.screens.BusesScreen
import com.ell.nammaooru.ui.screens.AddBusScreen
import com.ell.nammaooru.ui.screens.SettingsScreen
import com.ell.nammaooru.ui.theme.AppTheme
import com.ell.nammaooru.ui.widgets.TimeFilter

private enum class MainTab(val title: String, val icon: ImageVector) {
    UPCOMING("Next", Icons.Outlined.DirectionsBus),
    DAILY("Daily", Icons.Outlined.Schedule),
    ADD_BUS("Add bus", Icons.Outlined.Add),
    SETTINGS("Settings", Icons.Outlined.Settings)
}

@Composable
@Preview
fun App() {
    AppTheme {
        var selectedTab by remember { mutableStateOf(MainTab.UPCOMING) }
        var departureLocation by remember { mutableStateOf<String?>("Viriyur") }
        var destination by remember { mutableStateOf<String?>(null) }
        var timeFilter by remember { mutableStateOf(TimeFilter.ALL_DAY) }
        var showTamilNames by remember { mutableStateOf(true) }

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                NavigationBar {
                    MainTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            icon = { Icon(tab.icon, contentDescription = tab.title) },
                            label = { Text(tab.title) }
                        )
                    }
                }
            }
        ) { paddingValues ->
            when (selectedTab) {
                MainTab.UPCOMING,
                MainTab.DAILY -> BusesScreen(
                    showFullTimetable = selectedTab == MainTab.DAILY,
                    selectedDepartureLocation = departureLocation,
                    onDepartureLocationSelected = {
                        departureLocation = it
                        destination = null
                    },
                    selectedDestination = destination,
                    onDestinationSelected = { destination = it },
                    selectedTimeFilter = timeFilter,
                    onTimeFilterSelected = { timeFilter = it },
                    showTamilNames = showTamilNames,
                    onOpenFullTimetable = { selectedTab = MainTab.DAILY },
                    modifier = Modifier.padding(paddingValues)
                )

                MainTab.ADD_BUS -> AddBusScreen(
                    modifier = Modifier.padding(paddingValues)
                )

                MainTab.SETTINGS -> SettingsScreen(
                    departureLocation = departureLocation,
                    showTamilNames = showTamilNames,
                    onTamilNamesChanged = { showTamilNames = it },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            }


    }
}