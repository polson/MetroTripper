package com.philsoft.metrotripper.viewmodel

import com.google.android.gms.maps.model.LatLng
import com.philsoft.metrotripper.model.Stop

sealed class MainUiEffect {
    data class MoveCamera(val latLng: LatLng) : MainUiEffect()
    data class SelectMarker(val stop: Stop) : MainUiEffect()
    data class ShowError(val message: String) : MainUiEffect()
    object CloseDrawer : MainUiEffect()
}
