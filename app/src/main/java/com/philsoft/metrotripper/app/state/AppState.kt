package com.philsoft.metrotripper.app.state

import com.philsoft.metrotripper.model.Stop

data class AppState(
        val appUiEvent: AppUiEvent,
        val selectedStop: Stop? = null,
        val isSelectedStopSaved: Boolean = false,
        val visibleStops: List<Stop> = arrayListOf(),
        val savedStopsMap: LinkedHashMap<Long, Stop> = LinkedHashMap())




