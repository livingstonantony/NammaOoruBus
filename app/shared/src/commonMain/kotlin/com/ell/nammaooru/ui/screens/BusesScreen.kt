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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

    var selected by remember { mutableStateOf(TimeFilter.EVENING) }

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
            Column(
                modifier = Modifier
                    .fillMaxSize()
//                    .verticalScroll(rememberScrollState())
            ) {

                FilterBus(
                    modifier = Modifier
                        .fillMaxWidth(),
//                        .padding(16.dp),
                    selectedFilter = selected,
                    onFilterSelected = { selected = it }
                )
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
        }

        is UiState.Error -> {
            Text(
                text = state.message,
                modifier = modifier
            )
        }
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

