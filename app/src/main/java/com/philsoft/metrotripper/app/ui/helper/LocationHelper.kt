package com.philsoft.metrotripper.app.ui.helper

import android.content.Context
import com.philsoft.metrotripper.app.state.LocationUiEvent.InitialLocationUpdate
import com.philsoft.metrotripper.utils.map.LocationClient
import kotlinx.coroutines.flow.*

/**
 * Provides a flow for location update events.  Must be initialized in onCreate of an activity
 */
class LocationHelper(val context: Context, private val permissionGranted: Flow<Boolean>) {

    val locationEvents: Flow<InitialLocationUpdate> = setupLocationEvents()

    private fun setupLocationEvents(): Flow<InitialLocationUpdate> {
        return permissionGranted.flatMapLatest { granted ->
            if (granted) {
                LocationClient.locationUpdates(context)
            } else {
                emptyFlow()
            }
        }.map { locationResult -> InitialLocationUpdate(locationResult) }
    }
}
