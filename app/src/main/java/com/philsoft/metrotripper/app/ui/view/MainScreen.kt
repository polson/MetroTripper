package com.philsoft.metrotripper.app.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.rememberCameraPositionState
import com.philsoft.metrotripper.R
import com.philsoft.metrotripper.app.ui.theme.DarkGray
import com.philsoft.metrotripper.model.Trip
import com.philsoft.metrotripper.viewmodel.MainUiEffect
import com.philsoft.metrotripper.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, MapsComposeExperimentalApi::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    topBar: @Composable () -> Unit = {},
    onEffect: (MainUiEffect) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraPositionState = rememberCameraPositionState()
    var stopIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }
    val stopMarkerStates = remember { mutableMapOf<Long, MarkerState>() }

    var mapLoaded by remember { mutableStateOf(false) }
    var pendingCameraTarget by remember { mutableStateOf<LatLng?>(null) }
    var cameraRequestToken by remember { mutableIntStateOf(0) }
    var pendingSelectedStopId by remember { mutableStateOf<Long?>(null) }
    var selectMarkerToken by remember { mutableIntStateOf(0) }

    val mapStyleOptions = remember(context) {
        MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style)
    }
    val mapProperties = remember(mapStyleOptions, uiState.isLocationButtonEnabled) {
        MapProperties(
            mapStyleOptions = mapStyleOptions,
            isMyLocationEnabled = uiState.isLocationButtonEnabled
        )
    }
    val mapUiSettings = remember(uiState.isLocationButtonEnabled) {
        MapUiSettings(myLocationButtonEnabled = uiState.isLocationButtonEnabled)
    }

    val stopMarkers = remember(uiState.visibleStops, uiState.selectedStop) {
        buildList {
            val seen = hashSetOf<Long>()
            uiState.visibleStops.forEach { stop ->
                if (seen.add(stop.stopId)) {
                    add(stop)
                }
            }
            uiState.selectedStop?.let { selectedStop ->
                if (seen.add(selectedStop.stopId)) {
                    add(selectedStop)
                }
            }
        }
    }
    val stopMarkerIds = remember(stopMarkers) { stopMarkers.map { it.stopId } }

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            skipHiddenState = false
        )
    )
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(viewModel.uiEffect, lifecycleOwner.lifecycle) {
        viewModel.uiEffect
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .collect { effect ->
                when (effect) {
                    is MainUiEffect.MoveCamera -> {
                        pendingCameraTarget = effect.latLng
                        cameraRequestToken++
                    }
                    is MainUiEffect.SelectMarker -> {
                        pendingSelectedStopId = effect.stop.stopId
                        selectMarkerToken++
                    }
                    else -> onEffect(effect)
                }
            }
    }

    LaunchedEffect(cameraRequestToken, mapLoaded) {
        val target = pendingCameraTarget ?: return@LaunchedEffect
        if (!mapLoaded) {
            return@LaunchedEffect
        }
        cameraPositionState.animate(
            CameraUpdateFactory.newLatLngZoom(target, MainViewModel.MIN_ZOOM_LEVEL)
        )
    }

    LaunchedEffect(mapLoaded, cameraPositionState) {
        if (!mapLoaded) {
            return@LaunchedEffect
        }

        var wasMoving = false
        snapshotFlow { cameraPositionState.isMoving to cameraPositionState.position }
            .collect { (isMoving, position) ->
                if (isMoving) {
                    wasMoving = true
                } else if (wasMoving) {
                    wasMoving = false
                    viewModel.onCameraIdle(position)
                }
            }
    }

    LaunchedEffect(selectMarkerToken, mapLoaded, stopMarkerIds) {
        val stopId = pendingSelectedStopId ?: return@LaunchedEffect
        if (!mapLoaded) {
            return@LaunchedEffect
        }
        stopMarkerStates[stopId]?.showInfoWindow()
    }

    LaunchedEffect(mapLoaded) {
        if (mapLoaded && stopIcon == null) {
            stopIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_bus_stop_2)
        }
    }

    LaunchedEffect(uiState.selectedStop) {
        if (uiState.selectedStop != null) {
            scaffoldState.bottomSheetState.partialExpand()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val hasStop = uiState.selectedStop != null
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            topBar = topBar,
            sheetDragHandle = null,
            sheetShape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
            sheetContainerColor = if (hasStop) DarkGray else Color.Transparent,
            sheetShadowElevation = if (hasStop) 8.dp else 0.dp,
            sheetTonalElevation = if (hasStop) 0.dp else 0.dp,
            sheetPeekHeight = if (hasStop) 155.dp else 0.dp,
            sheetContent = {
                if (hasStop) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.safeDrawing)
                    ) {
                        StopHeading(
                            stop = uiState.selectedStop,
                            isSaved = uiState.isSelectedStopSaved,
                            isLoading = uiState.isLoadingTrips,
                            onScheduleClick = {
                                coroutineScope.launch { scaffoldState.bottomSheetState.expand() }
                                viewModel.onScheduleButtonClicked()
                            },
                            onLocationClick = {
                                coroutineScope.launch { scaffoldState.bottomSheetState.partialExpand() }
                                viewModel.onLocationButtonClicked()
                            },
                            onSaveClick = { viewModel.onSaveStopClicked() },
                            onHeaderClick = {
                                coroutineScope.launch {
                                    val sheetState = scaffoldState.bottomSheetState
                                    if (sheetState.currentValue == SheetValue.Expanded) {
                                        sheetState.partialExpand()
                                    } else {
                                        sheetState.expand()
                                    }
                                }
                            }
                        )
                        TripList(trips = uiState.trips)
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .fillMaxSize()
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = mapProperties,
                    uiSettings = mapUiSettings,
                    onMapLoaded = { mapLoaded = true }
                ) {
                    stopMarkers.forEach { stop ->
                        val markerState = stopMarkerStates.getOrPut(stop.stopId) {
                            MarkerState(position = stop.latLng)
                        }
                        if (markerState.position != stop.latLng) {
                            markerState.position = stop.latLng
                        }
                        Marker(
                            state = markerState,
                            title = stop.stopId.toString(),
                            icon = stopIcon,
                            onClick = {
                                viewModel.onMarkerClicked(stop.stopId)
                                false
                            }
                        )
                    }
                    val stopIdsSet = stopMarkerIds.toSet()
                    stopMarkerStates.keys
                        .filterNot(stopIdsSet::contains)
                        .forEach(stopMarkerStates::remove)

                    if (uiState.selectedStop != null) {
                        uiState.trips.forEach { trip ->
                            VehicleMarker(
                                trip = trip
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VehicleMarker(trip: Trip) {
    val label = remember(trip.route, trip.terminal) {
        "${trip.route}${trip.terminal}"
    }
    val position = remember(trip.vehicleLatitude, trip.vehicleLongitude) {
        LatLng(trip.vehicleLatitude.toDouble(), trip.vehicleLongitude.toDouble())
    }
    val markerState = remember {
        MarkerState(position = position)
    }
    if (markerState.position != position) {
        markerState.position = position
    }

    MarkerComposable(
        state = markerState,
        title = stringResource(R.string.vehicle) + " " + label,
        anchor = Offset(0f, 1f)
    ) {
        VehicleMarkerBadge(label = label)
    }
}

@Composable
private fun VehicleMarkerBadge(label: String) {
    val fillColor = colorResource(id = R.color.blue)
    val borderColor = colorResource(id = R.color.white)
    Box(
        modifier = Modifier
            .size(30.dp)
            .background(color = fillColor, shape = CircleShape)
            .border(width = 2.dp, color = borderColor, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = borderColor
        )
    }
}
