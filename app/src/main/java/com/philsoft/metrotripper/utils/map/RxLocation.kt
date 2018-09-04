package com.philsoft.metrotripper.utils.map

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable

class RxLocation {

    companion object {
        @SuppressLint("MissingPermission")
        fun locationEvents(client: FusedLocationProviderClient, rxPermissions: RxPermissions): Observable<LocationResult> {
            return Observable.create<LocationResult> { emitter ->
                val locationCallback: LocationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        emitter.onNext(locationResult)
                    }
                }

                rxPermissions.request(ACCESS_FINE_LOCATION)
                        .subscribe { granted ->
                            if (granted) {
                                client.requestLocationUpdates(LocationRequest.create(), locationCallback, Looper.myLooper())
                                emitter.setCancellable { client.removeLocationUpdates(locationCallback) }
                            }
                        }
            }
        }
    }
}
