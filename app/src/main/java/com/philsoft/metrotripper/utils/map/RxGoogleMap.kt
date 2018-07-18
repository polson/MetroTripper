package com.philsoft.metrotripper.utils.map

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Marker
import io.reactivex.Observable

class RxGoogleMap {

    companion object {
        fun cameraIdleEvents(map: GoogleMap): Observable<CameraPosition> {
            return Observable.create<CameraPosition> { emitter ->
                map.setOnCameraIdleListener {
                    emitter.onNext(map.cameraPosition)
                }
                emitter.setCancellable { map.setOnCameraIdleListener(null) }
            }.share()
        }

        fun markerClicks(map: GoogleMap): Observable<Marker> {
            return Observable.create<Marker> { emitter ->
                map.setOnMarkerClickListener {
                    marker -> emitter.onNext(marker)
                    false
                }
                emitter.setCancellable { map.setOnMarkerClickListener(null) }
            }.share()
        }
    }
}
