package com.ell.nammaooru.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.CarRental
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Timelapse
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Timer3
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


data class LocationRoute(val icon: ImageVector, val stop: String,val isSelected: Boolean= false)

val dummyList = mutableListOf<LocationRoute>(
    LocationRoute(Icons.Outlined.Apps, "All Buses", true),
    LocationRoute(Icons.Outlined.Navigation, "To Kallakurichi", false),
    LocationRoute(Icons.Outlined.Navigation, "To Sankrapuram",false),
    LocationRoute(Icons.Outlined.Navigation, "To Thiruvannamalai",false),
    LocationRoute(Icons.Outlined.CarRental, "Govt TNSTC",false),
    LocationRoute(Icons.Outlined.CarRental, "Private Express",false),
)

@Composable
fun DepartingFromCard() {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
//            .background(Color(0xFFEDEDED))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Row(
                        modifier = Modifier,
                        verticalAlignment = Alignment.CenterVertically


                    ) {
                        Icon(
                            Icons.Outlined.LocationOn,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "ORIGIN BAY",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Departing from Viriyur",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        "விரியூர் கிராமப் பேருந்து",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                TimeChipsCard(
                    icon = Icons.Outlined.Timer,
                    text = "12:30 AM"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            LazyRow {
                items(dummyList.size) {
                    LocationFilterChips(
                        icon = dummyList[it].icon,
                        text = dummyList[it].stop,
                        isSelected = dummyList[it].isSelected,
                        onClick = {
                            dummyList[it] = dummyList[it].copy(isSelected = !dummyList[it].isSelected)
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }
    }
}

@Composable
fun TimeChipsCard(icon: ImageVector, text: String) {

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))

            .background(Color.LightGray)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            icon,
            contentDescription = "Settings",
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
        )
    }
}


@Composable
fun LocationFilterChips(icon: ImageVector, text: String, isSelected: Boolean, onClick: () -> Unit) {

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))

            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            icon,
            contentDescription = "Settings",
            tint = if (isSelected) Color.White else Color.Gray,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = if (isSelected) Color.White else Color.Gray,
        )
    }
}

@Preview
@Composable
fun DepartingFromCardPreview() {
    Box(
        modifier = Modifier
            .background(Color(0xFFF7F7F7))
            .padding(16.dp)
    ) {
        DepartingFromCard()
    }

}
