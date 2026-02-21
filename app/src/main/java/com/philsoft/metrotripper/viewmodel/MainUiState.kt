package com.philsoft.metrotripper.viewmodel

import com.philsoft.metrotripper.model.Stop
import com.philsoft.metrotripper.model.Trip

data class MainUiState(
    val selectedStop: Stop? = null,
    val isSelectedStopSaved: Boolean = false,
    val trips: List<Trip> = emptyList(),
    val visibleStops: List<Stop> = emptyList(),
    val savedStops: Map<Long, Stop> = emptyMap(),
    val isLoadingTrips: Boolean = false,
    val isDrawerOpen: Boolean = false,
    val isLocationButtonEnabled: Boolean = false
)
