package com.philsoft.metrotripper.app.ui.helper

import com.philsoft.metrotripper.app.nextrip.NexTripService
import com.philsoft.metrotripper.app.state.NexTripAction
import com.philsoft.metrotripper.app.state.NexTripApiEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class NexTripApiHelper(private val scope: CoroutineScope) {
    private val nexTripService = NexTripService.create()
    private val _apiResultEvents = MutableSharedFlow<NexTripApiEvent>()
    val apiResultEvents = _apiResultEvents.asSharedFlow()

    fun render(action: NexTripAction) {
        when (action) {
            is NexTripAction.GetTrips -> getTrips(action.stopId)
        }
    }

    private fun getTrips(stopId: Long) {
        scope.launch {
            _apiResultEvents.emit(NexTripApiEvent.GetTripsInFlight)
            try {
                val trips = nexTripService.getTrips(stopId)
                _apiResultEvents.emit(NexTripApiEvent.GetTripsComplete(trips))
            } catch (e: Exception) {
                Timber.e(e, "Error getting trips")
                _apiResultEvents.emit(NexTripApiEvent.GetTripsFailed)
            }
        }
    }
}
