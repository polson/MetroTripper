package com.philsoft.metrotripper.app.ui.view

import com.philsoft.metrotripper.app.nextrip.NexTripService
import com.philsoft.metrotripper.app.state.AppUiEvent
import com.philsoft.metrotripper.app.state.NexTripAction
import com.philsoft.metrotripper.model.Trip
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.rxkotlin.subscribeBy

class NexTripApiHelper {
    private val nexTripService = NexTripService.create()
    private lateinit var emitter: ObservableEmitter<NexTripApiEvent>
    val apiResultEvents: Observable<NexTripApiEvent> = Observable.create<NexTripApiEvent> { emitter = it }.share()

    sealed class NexTripApiEvent : AppUiEvent() {
        object GetTripsInFlight : NexTripApiEvent()
        class GetTripsComplete(val trips: List<Trip>) : NexTripApiEvent()
        object GetTripsFailed : NexTripApiEvent()
    }

    fun render(action: NexTripAction) {
        when (action) {
            is NexTripAction.GetTrips -> getTrips(action.stopId)
        }
    }

    private fun getTrips(stopId: Long) {
        emitter.onNext(NexTripApiEvent.GetTripsInFlight)
        nexTripService.getTrips(stopId)
                .subscribeBy(onNext = { trips ->
                    emitter.onNext(NexTripApiEvent.GetTripsComplete(trips))
                }, onError = {
                    emitter.onNext(NexTripApiEvent.GetTripsFailed)
                })
    }
}
