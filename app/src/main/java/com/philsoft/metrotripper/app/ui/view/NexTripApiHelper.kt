package com.philsoft.metrotripper.app.ui.view

import com.philsoft.metrotripper.app.nextrip.NexTripService
import com.philsoft.metrotripper.app.state.AppUiEvent
import com.philsoft.metrotripper.app.state.NexTripAction
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class NexTripApiHelper {
    private val nexTripService = NexTripService.create()
    private lateinit var emitter: ObservableEmitter<AppUiEvent>

    val apiResultObservable: Observable<AppUiEvent> = Observable.create<AppUiEvent> { emitter = it }.share()

    fun render(action: NexTripAction) {
        when (action) {
            is NexTripAction.GetTrips -> getTrips(action.stopId)
        }
    }

    private fun getTrips(stopId: Long) {
        nexTripService.getTrips(stopId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { trips ->
                    emitter.onNext(AppUiEvent.GetTripsComplete(trips))
                }
    }
}
