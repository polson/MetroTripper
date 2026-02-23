package com.philsoft.metrotripper.app.ui.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.philsoft.metrotripper.R
import com.philsoft.metrotripper.app.ui.theme.DarkBlue
import com.philsoft.metrotripper.app.ui.theme.DarkGray
import com.philsoft.metrotripper.app.ui.theme.LightGray
import com.philsoft.metrotripper.model.Stop

val SidebarBg = Color(0xFF333333)
val DarkestGray = Color(0xFF222222)

@Composable
fun StopList(
    stops: List<Stop>,
    selectedStopId: Long,
    onStopSearched: (Long) -> Unit,
    onStopSelected: (Stop) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SidebarBg)
    ) {
        item {
            SearchDrawerItem(onSearch = onStopSearched)
        }
        items(stops, key = { it.stopId }) { stop ->
            StopDrawerItem(
                stop = stop,
                isSelected = stop.stopId == selectedStopId,
                onClick = { onStopSelected(stop) }
            )
        }
    }
}

@Composable
fun SearchDrawerItem(
    onSearch: (Long) -> Unit
) {
    var text by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = text,
                onValueChange = {
                    if (it.length <= 5 && it.all { char -> char.isDigit() }) {
                        text = it
                    }
                },
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        stringResource(R.string.enter_stop_number),
                        color = LightGray,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                textStyle = LocalTextStyle.current.copy(
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Go
                ),
                keyboardActions = KeyboardActions(
                    onGo = {
                        text.toLongOrNull()?.let { onSearch(it) }
                    }
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Color.White
                ),
                singleLine = true
            )

            IconButton(
                onClick = { text.toLongOrNull()?.let { onSearch(it) } },
                modifier = Modifier.padding(horizontal = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.White
                )
            }
        }

        HorizontalDivider(color = DarkestGray, thickness = 1.dp)
        HorizontalDivider(color = DarkGray, thickness = 1.dp)
    }
}

@Composable
fun StopDrawerItem(
    stop: Stop,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) DarkBlue else SidebarBg

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(20.dp),
        verticalAlignment = Alignment.Top
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_bus_stop_2),
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .padding(end = 15.dp)
        )

        Column {
            Text(
                text = stop.stopId.toString(),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stop.stopName,
                color = Color.White,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StopListPreview() {
    val mockStops = listOf(
        Stop(17025, "2nd St NE & 1st Ave NE", "2nd St NE at 1st Ave NE", 44.988, -93.254, 0, ""),
        Stop(17026, "Central Ave & Broadway", "Central Ave at Broadway St", 45.0, -93.0, 0, ""),
        Stop(17027, "Nicollet Mall & 5th St", "Nicollet Mall at 5th St", 44.978, -93.27, 0, "")
    )
    
    StopList(
        stops = mockStops,
        selectedStopId = 17026,
        onStopSearched = {},
        onStopSelected = {}
    )
}
