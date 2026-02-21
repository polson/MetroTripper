package com.philsoft.metrotripper.utils.map

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Marker
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

object FlowGoogleMap {

    fun cameraIdleEvents(map: GoogleMap): Flow<CameraPosition> = callbackFlow {
        map.setOnCameraIdleListener {
            trySend(map.cameraPosition)
        }
        awaitClose { map.setOnCameraIdleListener(null) }
    }

    fun markerClicks(map: GoogleMap): Flow<Marker> = callbackFlow {
        map.setOnMarkerClickListener { marker ->
            trySend(marker)
            false // Return false to indicate that the event is not consumed
        }
        awaitClose { map.setOnMarkerClickListener(null) }
    }
}
