package com.ell.nammaooru.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class TimeFilter(
    val title: String,
    val timeRange: String,
    val startMinute: Int,
    val endMinuteExclusive: Int
) {
    ALL_DAY("All day", "All departures", 0, 1440),
    MORNING("Morning", "05 AM - 12 PM", 300, 720),
    AFTERNOON("Afternoon", "12 PM - 05 PM", 720, 1020),
    EVENING("Evening", "05 PM - 10 PM", 1020, 1320)
}

@Composable
fun FilterBus(
    title: String = "Full Daily Timetable",
    tripCountText: String = "15 Daily Trips",
    selectedFilter: TimeFilter = TimeFilter.EVENING,
    onFilterSelected: (TimeFilter) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        // Header Row: Title and Trip Count
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    color =  Color(0xFF1E1E1E),
                    fontWeight = FontWeight.Bold
                ),
            )
            Text(
                text = tripCountText,
                style = MaterialTheme.typography.bodySmall.copy(
                    color =  Color(0xFF0F3E29),
                    fontWeight = FontWeight.SemiBold
                ),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Segmented Filter Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(50.dp))
                .background(Color(0xFFF2F4F3))
                .padding(2.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TimeFilter.entries.forEach { filter ->
                    val isSelected = filter == selectedFilter

                    val itemModifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50.dp))
                        .then(
                            if (isSelected) {
                                Modifier.background(Color(0xFF0F3E29))
                            } else {
                                Modifier
                            }
                        )
                        .clickable { onFilterSelected(filter) }
                        .padding(vertical = 2.dp, horizontal = 4.dp)

                    Box(
                        modifier = itemModifier,
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = filter.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else Color(0xFF2C2C2C)
                            )
//                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = filter.timeRange,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isSelected) Color.White.copy(alpha = 0.85f) else Color(0xFF757575)
                            )
                        }
                    }
                }
            }
        }
    }
}




@Preview
@Composable
fun FilterBusPreview() {
    var selected by remember { mutableStateOf(TimeFilter.EVENING) }
    Box(
        modifier = Modifier
            .background(Color.White)
            .padding(16.dp)
    ) {
        FilterBus(
            selectedFilter = selected,
            onFilterSelected = { selected = it }
        )
    }
}
