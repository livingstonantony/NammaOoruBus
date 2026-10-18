package com.ell.nammaooru.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ell.nammaooru.domain.model.Bus
import com.ell.nammaooru.domain.model.Days

/** Add form when [bus] is null, edit form otherwise. */
@Composable
fun BusFormScreen(
    bus: Bus?,
    saving: Boolean,
    serverError: String?,
    onSave: (Bus) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var departureLocation by remember { mutableStateOf(bus?.departingFrom ?: "Viriyur") }
    var destinationEnglish by remember { mutableStateOf(bus?.destinationEnglish.orEmpty()) }
    var destinationTamil by remember { mutableStateOf(bus?.destinationTamil.orEmpty()) }
    var busName by remember { mutableStateOf(bus?.busName.orEmpty()) }
    var note by remember { mutableStateOf(bus?.note.orEmpty()) }
    var departureTime by remember { mutableStateOf(bus?.departTime.orEmpty()) }
    var selectedDays by remember { mutableStateOf(bus?.days?.toSet() ?: Days.entries.toSet()) }
    var validationError by remember { mutableStateOf<String?>(null) }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(if (bus == null) "Add bus" else "Edit bus", style = MaterialTheme.typography.headlineSmall)

            OutlinedTextField(
                value = departureLocation,
                onValueChange = { departureLocation = it },
                label = { Text("Departing from") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = destinationEnglish,
                onValueChange = { destinationEnglish = it },
                label = { Text("Destination (English)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = destinationTamil,
                onValueChange = { destinationTamil = it },
                label = { Text("Destination (Tamil)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = busName,
                onValueChange = { busName = it },
                label = { Text("Bus name / route number") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Notes") },
                minLines = 2,
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = departureTime,
                onValueChange = { departureTime = it },
                label = { Text("Departure time (e.g. 05:45 AM)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Operating days", style = MaterialTheme.typography.titleSmall)
            Days.entries.chunked(4).forEach { daysInRow ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    daysInRow.forEach { day ->
                        FilterChip(
                            selected = day in selectedDays,
                            onClick = {
                                selectedDays = if (day in selectedDays) selectedDays - day else selectedDays + day
                            },
                            label = { Text(day.name.take(3)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            val error = validationError ?: serverError
            if (error != null) {
                Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Button(
                onClick = {
                    validationError = when {
                        departureLocation.isBlank() -> "Departing from is required"
                        destinationEnglish.isBlank() -> "English destination is required"
                        parseDepartureMinutes(departureTime) == null -> "Enter a valid time, e.g. 05:45 AM"
                        selectedDays.isEmpty() -> "Select at least one operating day"
                        else -> null
                    }
                    if (validationError == null) {
                        onSave(
                            Bus(
                                id = bus?.id.orEmpty(),
                                departingFrom = departureLocation.trim(),
                                destinationEnglish = destinationEnglish.trim(),
                                destinationTamil = destinationTamil.trim(),
                                busName = busName.trim(),
                                note = note.trim(),
                                departTime = departureTime.trim(),
                                days = Days.entries.filter { it in selectedDays }
                            )
                        )
                    }
                },
                enabled = !saving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (saving) "Saving..." else "Save bus")
            }
            OutlinedButton(onClick = onCancel, enabled = !saving, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel")
            }
        }
    }
}
