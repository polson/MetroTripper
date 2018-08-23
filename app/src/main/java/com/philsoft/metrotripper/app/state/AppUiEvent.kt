package com.philsoft.metrotripper.app.state

import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.CameraPosition
import com.philsoft.metrotripper.model.Stop
import com.philsoft.metrotripper.model.Trip

open class AppUiEvent {
    object Initialize : AppUiEvent()
}
