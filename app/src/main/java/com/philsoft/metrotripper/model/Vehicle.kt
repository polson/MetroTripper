package com.philsoft.metrotripper.model

import com.google.android.gms.maps.model.LatLng

data class Vehicle(val route: String,
                   val terminal: String,
                   val heading: Double,
                   val latitude: Double,
                   val longitude: Double) {

    val routeAndTerminal: String
        get() = route + terminal

    val latLng: LatLng
        get() = LatLng(latitude, longitude)
}
