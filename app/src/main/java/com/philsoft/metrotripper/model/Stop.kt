package com.philsoft.metrotripper.model

import com.google.android.gms.maps.model.LatLng


data class Stop(val stopId: Long, val stopName: String, val stopDesc: String, val stopLat: Double, val stopLon: Double, val wheelchairBoarding: Int, val stopUrl: String) {

    val latLng: LatLng
        get() = LatLng(stopLat!!, stopLon!!)
}
