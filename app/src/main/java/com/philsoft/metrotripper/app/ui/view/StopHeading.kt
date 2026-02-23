package com.philsoft.metrotripper.app.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.philsoft.metrotripper.R
import com.philsoft.metrotripper.app.ui.theme.DarkGray
import com.philsoft.metrotripper.app.ui.theme.Gray
import com.philsoft.metrotripper.app.ui.theme.LightGray
import com.philsoft.metrotripper.app.ui.theme.YellowSaved
import com.philsoft.metrotripper.model.Stop

@Composable
fun StopHeading(
    stop: Stop?,
    isSaved: Boolean,
    isLoading: Boolean,
    onScheduleClick: () -> Unit,
    onLocationClick: () -> Unit,
    onSaveClick: () -> Unit,
    onHeaderClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (stop == null) return

    Column(modifier = modifier.fillMaxWidth().clickable { onHeaderClick() }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkGray)
                .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 10.dp)
        ) {
            Text(
                text = "Stop ${stop.stopId}",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stop.stopName,
                color = Color.White
            )
        }

        HorizontalDivider(thickness = 1.dp, color = DarkGray)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Gray),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .size(48.dp)
                        .padding(12.dp),
                    color = Color.White,
                    strokeWidth = 3.dp
                )
            } else {
                IconButton(
                    onClick = onScheduleClick,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_clock),
                        contentDescription = "Schedule",
                        tint = Color.Unspecified
                    )
                }
            }

            IconButton(
                onClick = onLocationClick,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_location),
                    contentDescription = "Location",
                    tint = Color.Unspecified
                )
            }

            IconButton(
                onClick = onSaveClick,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_star),
                    contentDescription = "Save Stop",
                    tint = if (isSaved) YellowSaved else Color.Unspecified
                )
            }
        }
    }
}

@Preview
@Composable
fun StopHeadingPreview() {
    StopHeading(
        stop = Stop(
            stopId =1234,
            stopName ="Northbound 6 to Downtown",
            stopDesc ="Description",
            stopLat =44.0,
            stopLon =-93.0,
            wheelchairBoarding = 1,
            stopUrl="url"
        ),
        isSaved = true,
        isLoading = false,
        onScheduleClick = {},
        onLocationClick = {},
        onSaveClick = {},
        onHeaderClick = {}
    )
}
