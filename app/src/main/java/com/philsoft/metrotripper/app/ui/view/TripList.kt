package com.philsoft.metrotripper.app.ui.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.philsoft.metrotripper.R
import com.philsoft.metrotripper.app.nextrip.constants.Direction
import com.philsoft.metrotripper.app.ui.theme.Blue
import com.philsoft.metrotripper.app.ui.theme.DarkBlue
import com.philsoft.metrotripper.app.ui.theme.LightBlue
import com.philsoft.metrotripper.model.Trip

@Composable
fun TripList(trips: List<Trip>, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Blue)
    ) {
        items(trips) { trip ->
            TripItem(trip = trip)
        }
    }
}

@Composable
fun TripItem(trip: Trip) {
    val direction = try {
        Direction.valueOf(trip.routeDirection)
    } catch (e: Exception) {
        Direction.UNKNOWN
    }
    
    val iconRes = when (direction) {
        Direction.NORTHBOUND -> R.drawable.ic_up_arrow
        Direction.SOUTHBOUND -> R.drawable.ic_down_arrow
        Direction.EASTBOUND -> R.drawable.ic_right_arrow
        Direction.WESTBOUND -> R.drawable.ic_left_arrow
        else -> 0
    }

    val timeAndText = trip.departureText.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }
    val timeNumberStr = timeAndText.firstOrNull() ?: ""
    val showUnit = timeAndText.size > 1
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Blue)
    ) {
        // Top border
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(LightBlue)
                .align(Alignment.TopCenter)
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 20.dp, top = 5.dp, bottom = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            
            // Route texts on the left
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${trip.route}${trip.terminal}",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 17.dp, end = 17.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (iconRes != 0) {
                        Image(
                            painter = painterResource(id = iconRes),
                            contentDescription = "Direction",
                            modifier = Modifier
                                .size(14.dp)
                                .padding(end = 3.dp, top = 1.dp)
                        )
                    }
                    Text(
                        text = trip.description,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }

            // Time block on the right
            Column(
                modifier = Modifier.width(80.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = timeNumberStr,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                if (showUnit) {
                    Text(
                        text = stringResource(id = R.string.minutes),
                        color = Color.White,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.offset(y = (-5).dp)
                    )
                }
            }
        }
        
        // Bottom border
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(DarkBlue)
                .align(Alignment.BottomCenter)
        )
    }
}
