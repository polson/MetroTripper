package com.philsoft.metrotripper.utils.map

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable

class RxGoogleMap {

    companion object {
        fun cameraIdleEvents(map: GoogleMap): Observable<CameraPosition> {
            return CameraIdleObservable(map)
        }
    }

    class CameraIdleObservable(private val map: GoogleMap) : Observable<CameraPosition>() {
        override fun subscribeActual(observer: Observer<in CameraPosition>) {
            val listener = Listener(map, observer)
            observer.onSubscribe(listener)
            map.setOnCameraIdleListener(listener)
        }

        internal class Listener(private val map: GoogleMap, private val observer: Observer<in CameraPosition>) : MainThreadDisposable(), GoogleMap.OnCameraIdleListener {
            override fun onCameraIdle() {
                if (!isDisposed) {
                    observer.onNext(map.cameraPosition)
                }
            }

            override fun onDispose() {
                map.setOnCameraIdleListener(null)
            }
        }
    }
}