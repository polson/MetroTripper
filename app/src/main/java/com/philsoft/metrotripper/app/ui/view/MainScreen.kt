package com.philsoft.metrotripper.app.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.philsoft.metrotripper.app.ui.theme.DarkGray
import com.philsoft.metrotripper.viewmodel.MainUiEffect
import com.philsoft.metrotripper.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, MapsComposeExperimentalApi::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    mapReadyCallback: OnMapReadyCallback,
    topBar: @Composable () -> Unit = {},
    onEffect: (MainUiEffect) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            skipHiddenState = false
        )
    )
    val coroutineScope = rememberCoroutineScope()

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(viewModel.uiEffect, lifecycleOwner.lifecycle) {
        viewModel.uiEffect
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .collect { effect ->
                onEffect(effect)
            }
    }

    // Show the sheet after recomposition has set the correct peek height
    LaunchedEffect(uiState.selectedStop) {
        if (uiState.selectedStop != null) {
            scaffoldState.bottomSheetState.partialExpand()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
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
                modifier = Modifier.fillMaxSize()
            ) {
                MapEffect { map ->
                    mapReadyCallback.onMapReady(map)
                }
            }
        }
    }
    } // end Box
}
