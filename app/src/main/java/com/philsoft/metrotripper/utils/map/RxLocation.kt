package com.philsoft.metrotripper.utils.map

import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import io.reactivex.Observable

class RxLocation() {

    companion object {
        fun locationEvents(client: FusedLocationProviderClient): Observable<LocationResult> {
            return Observable.create<LocationResult> { emitter ->
                val locationCallback: LocationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        emitter.onNext(locationResult)
                    }
                }
                client.requestLocationUpdates(LocationRequest.create(), locationCallback, Looper.myLooper())
                emitter.setCancellable { client.removeLocationUpdates(locationCallback) }
            }
        }
    }
}