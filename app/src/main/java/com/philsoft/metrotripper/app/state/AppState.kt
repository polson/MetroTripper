package com.philsoft.metrotripper.app.state

import com.philsoft.metrotripper.model.Stop
import com.philsoft.metrotripper.model.Trip

data class AppState(
        val selectedStop: Stop?,
        val isLoadingSchedule: Boolean,
        val isSelectedStopSaved: Boolean,
        val currentTrips: List<Trip>) {

    companion object {
        val initial = AppState(
                selectedStop = null,
                isLoadingSchedule = false,
                isSelectedStopSaved = false,
                currentTrips = arrayListOf())

    }
}
