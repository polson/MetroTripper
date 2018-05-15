package com.philsoft.metrotripper.app.ui.view

import com.philsoft.metrotripper.app.nextrip.NexTripService
import com.philsoft.metrotripper.app.state.AppUiEvent
import com.philsoft.metrotripper.app.state.NexTripAction
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class NexTripApiHelper {
    private val nexTripService = NexTripService.create()

    fun render(action: NexTripAction) {
        when (action) {
            is NexTripAction.GetTrips -> getTrips(action.stopId)
        }
    }

    fun getTrips(stopId: Long): Observable<AppUiEvent> {
        return nexTripService.getTrips(stopId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map<AppUiEvent> { trips ->
                    AppUiEvent.GetTripsComplete(trips)
                }
                .onErrorReturn {
                    AppUiEvent.GetTripsFailed
                }
    }
}
