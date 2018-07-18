package com.philsoft.metrotripper.app.ui.view

import com.philsoft.metrotripper.app.nextrip.NexTripService
import com.philsoft.metrotripper.app.state.NexTripAction
import com.philsoft.metrotripper.model.Trip
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

class NexTripApiHelper {
    private val nexTripService = NexTripService.create()
    private lateinit var emitter: ObservableEmitter<Event>
    val apiResultObservable: Observable<Event> = Observable.create<Event> { emitter = it }.share()

    sealed class Event {
        class LoadTripsComplete(val trips: List<Trip>) : Event()
        object LoadTripsFailed : Event()
        object LoadTripsInFlight : Event()
    }

    fun render(action: NexTripAction) {
        when (action) {
            is NexTripAction.GetTrips -> getTrips(action.stopId)
        }
    }

    private fun getTrips(stopId: Long) {
        emitter.onNext(Event.LoadTripsInFlight)
        nexTripService.getTrips(stopId)
                .subscribeOn(Schedulers.io())
                .subscribeBy(onNext = { trips ->
                    emitter.onNext(Event.LoadTripsComplete(trips))
                }, onError = {
                    emitter.onNext(Event.LoadTripsFailed)
                })
    }
}
