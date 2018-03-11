package com.philsoft.metrotripper.app.state

import android.content.Context
import com.philsoft.metrotripper.app.nextrip.NexTripService
import com.philsoft.metrotripper.database.DataProvider
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class AppStateManager(val context: Context) {

    private val stateSubject = PublishSubject.create<AppState>()
    private val nexTripService = NexTripService.create()
    private val dataProvider = DataProvider(context)
    //Automatically publish any changes to state var
    private val initialState: AppState = AppState(
            selectedStop = null,
            isLoadingSchedule = false,
            isSelectedStopSaved = false,
            currentTrips = arrayListOf())

    var state = initialState
        set(newState) {
            field = newState
            stateSubject.onNext(state)
        }


    val stateObservable: Observable<AppState> = stateSubject.hide()

    fun handleEvent(appEvent: AppEvent) {
        val something = when (appEvent) {
            AppEvent.ShowStops -> handleShowStops()
            AppEvent.ShowCurrentStopLocation -> handleShowCurrentStopLocation()
            is AppEvent.ShowSchedule -> handleShowSchedule()
            is AppEvent.SearchStop -> handleSearchStop(appEvent.stopId)
        }
    }

    private fun handleSearchStop(stopId: Long) {
        val stop = dataProvider.getStopById(stopId)
        state = state.copy(selectedStop = stop)
    }

    private fun handleShowStops() {
    }

    private fun handleShowSchedule() {
        val selectedStop = state.selectedStop
        if (selectedStop != null) {
            state = state.copy(isLoadingSchedule = true)
            nexTripService.getTrips(selectedStop.stopId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError { throwable ->
                        Timber.d("Unable to get trips: ${throwable.message}")
                        state = state.copy(isLoadingSchedule = false)
                    }
                    .subscribe { trips ->
                        state = state.copy(currentTrips = trips, isLoadingSchedule = false)
                    }
        }
    }


    private fun handleShowCurrentStopLocation() {

    }
}