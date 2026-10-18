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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ell.nammaooru.domain.model.Days

@Composable
fun AddBusScreen(modifier: Modifier = Modifier) {
    var departureLocation by remember { mutableStateOf("Viriyur") }
    var destinationEnglish by remember { mutableStateOf("") }
    var destinationTamil by remember { mutableStateOf("") }
    var busName by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var departureTime by remember { mutableStateOf("") }
    var selectedDays by remember { mutableStateOf(Days.entries.toSet()) }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Add bus", style = MaterialTheme.typography.headlineSmall)
            Text("VIRIYUR TRANSIT", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)

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

            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "Saving buses is unavailable until the server verifies an admin identity. The shared API key does not establish an admin role.",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
                Text("Save bus")
            }
            OutlinedButton(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
                Text("Save and add another")
            }
        }
    }
}