package com.philsoft.metrotripper.app.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

internal class MainMapRuntimeState internal constructor(
    val cameraPositionState: CameraPositionState
) {
    var mapLoaded by mutableStateOf(false)
    var stopIcon by mutableStateOf<BitmapDescriptor?>(null)
    val stopMarkerStates: MutableMap<Long, MarkerState> = mutableMapOf()

    var pendingCameraTarget by mutableStateOf<LatLng?>(null)
    var pendingSelectedStopId by mutableStateOf<Long?>(null)

    var cameraRequestToken by mutableIntStateOf(0)
    var selectMarkerToken by mutableIntStateOf(0)
}

@Composable
internal fun rememberMainMapRuntimeState(): MainMapRuntimeState {
    val cameraPositionState = rememberCameraPositionState()
    return remember(cameraPositionState) {
        MainMapRuntimeState(cameraPositionState = cameraPositionState)
    }
}
