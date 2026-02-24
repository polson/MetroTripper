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
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.fragment.app.FragmentManager
import com.google.android.gms.maps.OnMapReadyCallback
import com.philsoft.metrotripper.R
import com.philsoft.metrotripper.app.ui.theme.DarkGray
import com.philsoft.metrotripper.databinding.LayoutMapBinding
import com.philsoft.metrotripper.viewmodel.MainUiEffect
import com.philsoft.metrotripper.viewmodel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    supportFragmentManager: FragmentManager,
    mapReadyCallback: OnMapReadyCallback,
    topBar: @Composable () -> Unit = {},
    onEffect: (MainUiEffect, BottomSheetScaffoldState, CoroutineScope) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            skipHiddenState = false
        )
    )
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            onEffect(effect, scaffoldState, coroutineScope)
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
                        onScheduleClick = { viewModel.onScheduleButtonClicked() },
                        onLocationClick = { viewModel.onLocationButtonClicked() },
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
            AndroidViewBinding(LayoutMapBinding::inflate) {
                val mapFragment =
                    supportFragmentManager.findFragmentById(R.id.map) as? com.google.android.gms.maps.SupportMapFragment
                mapFragment?.getMapAsync(mapReadyCallback)
            }
        }
    }
    } // end Box
}
