@file:OptIn(ExperimentalMaterial3Api::class)

package com.philsoft.metrotripper.app.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.philsoft.metrotripper.R
import com.philsoft.metrotripper.app.ui.theme.DarkGray
import com.philsoft.metrotripper.model.Stop
import com.philsoft.metrotripper.viewmodel.MainUiEffect
import com.philsoft.metrotripper.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    topBar: @Composable () -> Unit = {},
    onEffect: (MainUiEffect) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val runtimeState = rememberMainMapRuntimeState()

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
        buildStopMarkers(
            visibleStops = uiState.visibleStops,
            selectedStop = uiState.selectedStop
        )
    }
    val stopMarkerIds = remember(stopMarkers) { stopMarkers.map { it.stopId } }

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            skipHiddenState = false
        )
    )
    val coroutineScope = rememberCoroutineScope()

    ObserveUiEffects(
        viewModel = viewModel,
        lifecycleOwner = lifecycleOwner,
        runtimeState = runtimeState,
        onEffect = onEffect
    )
    HandleCameraMoveRequests(runtimeState = runtimeState)
    HandleCameraIdleEvents(
        viewModel = viewModel,
        runtimeState = runtimeState
    )
    HandleMarkerSelectionRequests(
        runtimeState = runtimeState,
        stopMarkerIds = stopMarkerIds
    )
    HandleStopIconLoading(runtimeState = runtimeState)
    HandleSelectedStopSheetState(
        selectedStop = uiState.selectedStop,
        scaffoldState = scaffoldState
    )

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
                MainBottomSheetContent(
                    uiState = uiState,
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
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .fillMaxSize()
            ) {
                MainMapContent(
                    uiState = uiState,
                    runtimeState = runtimeState,
                    mapProperties = mapProperties,
                    mapUiSettings = mapUiSettings,
                    stopMarkers = stopMarkers,
                    stopMarkerIds = stopMarkerIds,
                    onMarkerClick = viewModel::onMarkerClicked,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun ObserveUiEffects(
    viewModel: MainViewModel,
    lifecycleOwner: LifecycleOwner,
    runtimeState: MainMapRuntimeState,
    onEffect: (MainUiEffect) -> Unit
) {
    LaunchedEffect(viewModel.uiEffect, lifecycleOwner.lifecycle) {
        viewModel.uiEffect
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .collect { effect ->
                when (effect) {
                    is MainUiEffect.MoveCamera -> {
                        runtimeState.pendingCameraTarget = effect.latLng
                        runtimeState.cameraRequestToken++
                    }

                    is MainUiEffect.SelectMarker -> {
                        runtimeState.pendingSelectedStopId = effect.stop.stopId
                        runtimeState.selectMarkerToken++
                    }

                    else -> onEffect(effect)
                }
            }
    }
}

@Composable
private fun HandleCameraMoveRequests(runtimeState: MainMapRuntimeState) {
    LaunchedEffect(runtimeState.cameraRequestToken, runtimeState.mapLoaded) {
        val target = runtimeState.pendingCameraTarget ?: return@LaunchedEffect
        if (!runtimeState.mapLoaded) {
            return@LaunchedEffect
        }

        runtimeState.cameraPositionState.animate(
            CameraUpdateFactory.newLatLngZoom(target, MainViewModel.MIN_ZOOM_LEVEL)
        )
    }
}

@Composable
private fun HandleCameraIdleEvents(
    viewModel: MainViewModel,
    runtimeState: MainMapRuntimeState
) {
    LaunchedEffect(runtimeState.mapLoaded, runtimeState.cameraPositionState) {
        if (!runtimeState.mapLoaded) {
            return@LaunchedEffect
        }

        var wasMoving = false
        snapshotFlow { runtimeState.cameraPositionState.isMoving to runtimeState.cameraPositionState.position }
            .collect { (isMoving, position) ->
                if (isMoving) {
                    wasMoving = true
                } else if (wasMoving) {
                    wasMoving = false
                    viewModel.onCameraIdle(position)
                }
            }
    }
}

@Composable
private fun HandleMarkerSelectionRequests(
    runtimeState: MainMapRuntimeState,
    stopMarkerIds: List<Long>
) {
    LaunchedEffect(runtimeState.selectMarkerToken, runtimeState.mapLoaded, stopMarkerIds) {
        val stopId = runtimeState.pendingSelectedStopId ?: return@LaunchedEffect
        if (!runtimeState.mapLoaded) {
            return@LaunchedEffect
        }

        runtimeState.stopMarkerStates[stopId]?.showInfoWindow()
    }
}

@Composable
private fun HandleStopIconLoading(runtimeState: MainMapRuntimeState) {
    LaunchedEffect(runtimeState.mapLoaded) {
        if (runtimeState.mapLoaded && runtimeState.stopIcon == null) {
            runtimeState.stopIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_bus_stop_2)
        }
    }
}

@Composable
private fun HandleSelectedStopSheetState(
    selectedStop: Stop?,
    scaffoldState: BottomSheetScaffoldState
) {
    LaunchedEffect(selectedStop) {
        if (selectedStop != null) {
            scaffoldState.bottomSheetState.partialExpand()
        }
    }
}

internal fun buildStopMarkers(visibleStops: List<Stop>, selectedStop: Stop?): List<Stop> {
    return buildList {
        val seen = hashSetOf<Long>()
        visibleStops.forEach { stop ->
            if (seen.add(stop.stopId)) {
                add(stop)
            }
        }

        if (selectedStop != null && seen.add(selectedStop.stopId)) {
            add(selectedStop)
        }
    }
}
