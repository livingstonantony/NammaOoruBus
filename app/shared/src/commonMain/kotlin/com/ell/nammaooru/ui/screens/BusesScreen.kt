package com.ell.nammaooru.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.ell.nammaooru.domain.model.Bus
import com.ell.nammaooru.domain.model.Days
import com.ell.nammaooru.feature.bus.BusIntent
import com.ell.nammaooru.feature.bus.BusViewModel
import com.ell.nammaooru.feature.bus.UiState
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BusesScreen(
    buses: List<Bus> = sampleBuses,
    modifier: Modifier = Modifier,
    viewModel: BusViewModel = koinViewModel()
) {

    val uiState by viewModel.stateBuses.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.processIntent(BusIntent.GetBuses)
    }

    LaunchedEffect(uiState) {
        println("STATE: $uiState")
    }
    when (val state = uiState) {
        is UiState.Loading -> {
            CircularProgressIndicator()
        }

        is UiState.Success -> {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color(0xFFF7F7F7)),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = state.data,
                    key = { it.id }
                ) { bus ->
                    BusCard(bus = bus)
                }
            }
        }

        is UiState.Error -> {
            Text(
                text = state.message,
                modifier = modifier
            )
        }
    }
}

@Composable
fun BusCard(
    bus: Bus,
    modifier: Modifier = Modifier
) {
    val (timeText, amPmText) = parseDepartTime(bus.departTime)
    val formattedDays = formatDays(bus.days)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),

        ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Time Container
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFEDEDED))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = timeText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E1E)
                    )
                    Text(
                        text = amPmText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF6E6E6E)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Middle: Destination and Bus Details
            Column(
                modifier = Modifier.weight(1f)
                    ,
                verticalArrangement = Arrangement.Center,
            ) {


                val titleText = buildAnnotatedString {
                    if (bus.destinationEnglish.isNotBlank()) {
                        withStyle(
                            style = SpanStyle(
                                color = Color(0xFF1E1E1E),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        ) {
                            append(bus.destinationEnglish)
                        }
                    }


                    if (bus.destinationTamil.isNotBlank()) {
                        if (bus.destinationEnglish.isNotBlank()) {
                            append(" ")
                        }
                        withStyle(
                            style = SpanStyle(
                                color = Color(0xFF6E6E6E),
                                fontWeight = FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        ) {
                            append(bus.destinationTamil)
                        }
                    }
                }

                if (titleText.isNotEmpty()) {
                    Text(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        text = titleText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }


                Spacer(modifier = Modifier.height(4.dp))

                val subtitleText = buildAnnotatedString {
                    if (bus.busName.isNotBlank()) {
                        withStyle(
                            style = SpanStyle(
                                color = Color(0xFF135A3A),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        ) {
                            append(bus.busName)
                        }
                    }

                    if (bus.note.isNotBlank()) {
                        if (bus.busName.isNotBlank()) {
                            append(" ")
                        }
                        withStyle(
                            style = SpanStyle(
                                color = Color(0xFF6E6E6E),
                                fontWeight = FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        ) {
                            append(bus.note)
                        }
                    }
                }

                if (subtitleText.isNotEmpty()) {
                    Text(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        text = subtitleText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right: Days Pill
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFFE8ECE9))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = formattedDays,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF4A4A4A)
                )
            }
        }
    }
}

private fun parseDepartTime(departTime: String): Pair<String, String> {
    val trimmed = departTime.trim()
    val parts = trimmed.split("\\s+".toRegex())
    return if (parts.size >= 2) {
        Pair(parts[0], parts[1].uppercase())
    } else if (trimmed.contains("AM", ignoreCase = true)) {
        Pair(trimmed.replace("AM", "", ignoreCase = true).trim(), "AM")
    } else if (trimmed.contains("PM", ignoreCase = true)) {
        Pair(trimmed.replace("PM", "", ignoreCase = true).trim(), "PM")
    } else {
        Pair(trimmed, "AM")
    }
}

private fun formatDays(days: List<Days>): String {
    if (days.isEmpty() || days.size == Days.entries.size) return "Daily"
    val weekdays = listOf(Days.Monday, Days.Tuesday, Days.Wednesday, Days.Thursday, Days.Friday)
    if (days.size == 5 && days.containsAll(weekdays)) return "Mon - Fri"
    val weekend = listOf(Days.Saturday, Days.Sunday)
    if (days.size == 2 && days.containsAll(weekend)) return "Weekends"
    return days.joinToString(", ") { it.name.take(3) }
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

@Preview
@Composable
fun BusCardPreview() {
    Box(
        modifier = Modifier
            .background(Color(0xFFF7F7F7))
            .padding(16.dp)
    ) {
        BusCard(
            bus = sampleBuses[0]
        )
    }
}
