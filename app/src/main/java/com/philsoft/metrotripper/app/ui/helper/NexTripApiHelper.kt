package com.philsoft.metrotripper.app.ui.helper

import com.philsoft.metrotripper.app.nextrip.NexTripService
import com.philsoft.metrotripper.app.state.NexTripAction
import com.philsoft.metrotripper.app.state.NexTripApiEvent
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

class NexTripApiHelper {
    private val nexTripService = NexTripService.create()
    private lateinit var emitter: ObservableEmitter<NexTripApiEvent>
    val apiResultEvents: Observable<NexTripApiEvent> = Observable.create<NexTripApiEvent> { emitter = it }.share()

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
