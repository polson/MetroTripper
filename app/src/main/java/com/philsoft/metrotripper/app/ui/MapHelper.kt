package com.philsoft.metrotripper.app.ui

import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng

class MapHelper(private val map: GoogleMap) {

    fun saveState(outState: Bundle) {
        outState.putParcelable(KEY_CAMERA_POSITION, map.cameraPosition)
    }

    fun restoreState(savedState: Bundle) {
        val cameraPosition = savedState.getParcelable<CameraPosition>(KEY_CAMERA_POSITION)
        val cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition)
        map.moveCamera(cameraUpdate)
    }

    fun centerCameraOnLatLng(latLng: LatLng, animate: Boolean) {
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17f)
        if (animate) {
            map.animateCamera(cameraUpdate)
        } else {
            map.moveCamera(cameraUpdate)
        }
    }

    companion object {
        private val KEY_CAMERA_POSITION = "KEY_CAMERA_POSITION"
    }
}
