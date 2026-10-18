package com.ell.nammaooru

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ell.nammaooru.domain.model.AppMode
import com.ell.nammaooru.domain.model.Bus
import com.ell.nammaooru.feature.auth.AuthViewModel
import com.ell.nammaooru.feature.bus.BusIntent
import com.ell.nammaooru.feature.bus.BusViewModel
import com.ell.nammaooru.feature.bus.OperationState
import com.ell.nammaooru.ui.screens.BusFormScreen
import com.ell.nammaooru.ui.screens.BusesScreen
import com.ell.nammaooru.ui.screens.LoginScreen
import com.ell.nammaooru.ui.screens.SettingsScreen
import com.ell.nammaooru.ui.theme.AppTheme
import com.ell.nammaooru.ui.widgets.TimeFilter
import org.koin.compose.viewmodel.koinViewModel

private enum class MainTab(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Outlined.Home),
    SETTINGS("Settings", Icons.Outlined.Settings)
}

@Composable
@Preview
fun App(mode: AppMode = AppMode.Guest) {
    AppTheme {
        val isAdmin = mode == AppMode.Admin
        val authViewModel: AuthViewModel = koinViewModel()
        val busViewModel: BusViewModel = koinViewModel()
        val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
        val loginState by authViewModel.loginState.collectAsState()
        val operation by busViewModel.stateOperation.collectAsState()

        var selectedTab by remember { mutableStateOf(MainTab.HOME) }
        var departureLocation by remember { mutableStateOf<String?>("Viriyur") }
        var destination by remember { mutableStateOf<String?>(null) }
        var timeFilter by remember { mutableStateOf(TimeFilter.ALL_DAY) }
        var showTamilNames by remember { mutableStateOf(true) }
        var showForm by remember { mutableStateOf(false) }
        var editingBus by remember { mutableStateOf<Bus?>(null) }
        var busPendingDelete by remember { mutableStateOf<Bus?>(null) }
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(isLoggedIn) {
            if (!isLoggedIn) {
                showForm = false
                selectedTab = MainTab.HOME
            }
        }
        LaunchedEffect(operation) {
            when (val op = operation) {
                is OperationState.Success -> {
                    showForm = false
                    snackbarHostState.showSnackbar(op.message)
                    busViewModel.clearOperation()
                }
                is OperationState.Error -> if (!showForm) {
                    snackbarHostState.showSnackbar(op.message)
                    busViewModel.clearOperation()
                }
                else -> Unit
            }
        }

        if (isAdmin && !isLoggedIn) {
            Scaffold(containerColor = MaterialTheme.colorScheme.background) { paddingValues ->
                LoginScreen(
                    loading = loginState.loading,
                    error = loginState.error,
                    onLogin = authViewModel::login,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            return@AppTheme
        }

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                NavigationBar {
                    MainTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = {
                                selectedTab = tab
                                showForm = false
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.title) },
                            label = { Text(tab.title) }
                        )
                    }
                }
            },
            floatingActionButton = {
                if (isAdmin && selectedTab == MainTab.HOME && !showForm) {
                    FloatingActionButton(onClick = {
                        editingBus = null
                        busViewModel.clearOperation()
                        showForm = true
                    }) {
                        Icon(Icons.Outlined.Add, contentDescription = "Add bus")
                    }
                }
            }
        ) { paddingValues ->
            val contentModifier = Modifier.padding(paddingValues)
            when {
                selectedTab == MainTab.SETTINGS -> SettingsScreen(
                    departureLocation = departureLocation,
                    showTamilNames = showTamilNames,
                    onTamilNamesChanged = { showTamilNames = it },
                    isAdmin = isAdmin,
                    isLoggedIn = isLoggedIn,
                    onLogout = authViewModel::logout,
                    modifier = contentModifier
                )

                showForm -> BusFormScreen(
                    bus = editingBus,
                    saving = operation is OperationState.Loading,
                    serverError = (operation as? OperationState.Error)?.message,
                    onSave = { bus ->
                        val id = editingBus?.id
                        busViewModel.processIntent(
                            if (id == null) BusIntent.CreateBus(bus) else BusIntent.UpdateBus(id, bus)
                        )
                    },
                    onCancel = {
                        busViewModel.clearOperation()
                        showForm = false
                    },
                    modifier = contentModifier
                )

                else -> Column(modifier = contentModifier) {
                    BusesScreen(
                        showFullTimetable = true,
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
                        onOpenFullTimetable = {},
                        isAdmin = isAdmin,
                        onEditBus = {
                            editingBus = it
                            busViewModel.clearOperation()
                            showForm = true
                        },
                        onDeleteBus = { busPendingDelete = it },
                        viewModel = busViewModel
                    )
                }
            }
        }

        busPendingDelete?.let { bus ->
            AlertDialog(
                onDismissRequest = { busPendingDelete = null },
                title = { Text("Delete bus?") },
                text = { Text("${bus.busName} to ${bus.destinationEnglish} at ${bus.departTime} will be removed.") },
                confirmButton = {
                    TextButton(onClick = {
                        busViewModel.processIntent(BusIntent.DeleteBus(bus.id))
                        busPendingDelete = null
                    }) { Text("Delete") }
                },
                dismissButton = {
                    TextButton(onClick = { busPendingDelete = null }) { Text("Cancel") }
                }
            )
        }
    }
}
