package com.philsoft.metrotripper.app.nextrip

import com.philsoft.metrotripper.model.Trip
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class NexTripManager {
    val nexTripService = NexTripService.create()

    fun getTrips(stopId: Long): Observable<List<Trip>> {
        return nexTripService.getTrips(stopId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { throwable ->
                    Timber.d("Unable to get trips: ${throwable.message}")
                }
    }
}