package com.ell.nammaooru.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    departureLocation: String?,
    showTamilNames: Boolean,
    onTamilNamesChanged: (Boolean) -> Unit,
    isAdmin: Boolean,
    isLoggedIn: Boolean,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall)

        Surface(color = MaterialTheme.colorScheme.surfaceContainerLowest) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Show Tamil destinations", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Display Tamil names on timetable entries",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = showTamilNames, onCheckedChange = onTamilNamesChanged)
            }
        }

        Surface(color = MaterialTheme.colorScheme.surfaceContainerLowest) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Departure location", style = MaterialTheme.typography.titleSmall)
                Text(
                    departureLocation ?: "All locations",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (isAdmin && isLoggedIn) {
            Button(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
                Text("Log out")
            }
        }
    }
}