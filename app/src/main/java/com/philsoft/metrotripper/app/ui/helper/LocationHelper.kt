package com.philsoft.metrotripper.app.ui.helper

import android.content.Context
import com.google.android.gms.location.LocationServices
import com.philsoft.metrotripper.app.state.LocationUiEvent.InitialLocationUpdate
import com.philsoft.metrotripper.utils.map.RxLocation
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable

/**
 * Provides an observable for location update events.  Must be initialized in onCreate of an activity
 */
class LocationHelper(val context: Context, val rxPermissions: RxPermissions) {

    val locationEvents = setupLocationEvents()

    private fun setupLocationEvents(): Observable<InitialLocationUpdate> {
        val client = LocationServices.getFusedLocationProviderClient(context)
        return RxLocation.locationEvents(client, rxPermissions).map { locationResult -> InitialLocationUpdate(locationResult) }.share()
    }
}
