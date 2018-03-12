package com.philsoft.metrotripper.app.state

import android.content.Context
import com.google.android.gms.maps.model.CameraPosition
import com.philsoft.metrotripper.app.nextrip.NexTripService
import com.philsoft.metrotripper.database.DataProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class AppStateManager(val context: Context) {

    private val mapActionSubject = PublishSubject.create<MapAction>()
    private val stopHeadingActionSubject = PublishSubject.create<StopHeadingAction>()
    private val tripListActionSubject = PublishSubject.create<TripListAction>()

    //Outputs
    val mapActions = mapActionSubject.hide()
    val stopHeadingActions = stopHeadingActionSubject.hide()
    val tripListActions = tripListActionSubject.hide()

    private val nexTripService = NexTripService.create()
    private val dataProvider = DataProvider(context)
    private var state = AppState(
            selectedStop = null
    )

    companion object {
        private val MAX_STOPS = 20
    }

    fun handleEvent(appEvent: AppEvent) {
        val something = when (appEvent) {
            AppEvent.ShowStops -> handleShowStops()
            AppEvent.ShowCurrentStopLocation -> handleShowCurrentStopLocation()
            is AppEvent.ShowSchedule -> handleShowSchedule()
            is AppEvent.SearchStop -> handleSearchStop(appEvent.stopId)
            is AppEvent.CameraIdle -> handleCameraIdle(appEvent.cameraPosition)
        }
    }

    private fun handleCameraIdle(position: CameraPosition) {
        val stops = dataProvider.getClosestStops(position.target.latitude, position.target.longitude, MAX_STOPS)
        mapActionSubject.onNext(MapAction.ShowStopMarkers(stops))
    }

    private fun handleSearchStop(stopId: Long) {
        val stop = dataProvider.getStopById(stopId)
        if (stop != null) {
            state = state.copy(selectedStop = stop)
            stopHeadingActionSubject.onNext(StopHeadingAction.ShowStop(stop, false))
        }
    }

    private fun handleShowStops() {
    }

    private fun handleShowSchedule() {
        val selectedStop = state.selectedStop
        if (selectedStop != null) {
            stopHeadingActionSubject.onNext(StopHeadingAction.LoadingTrips)
            nexTripService.getTrips(selectedStop.stopId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError { throwable ->
                        Timber.d("Unable to get trips: ${throwable.message}")
                        stopHeadingActionSubject.onNext(StopHeadingAction.LoadTripsError())
                    }
                    .subscribe { trips ->
                        stopHeadingActionSubject.onNext(StopHeadingAction.LoadTripsComplete())
                        tripListActionSubject.onNext(TripListAction.ShowTrips(trips))
                    }
        }
    }


    private fun handleShowCurrentStopLocation() {
        val selectedStop = state.selectedStop
        if (selectedStop != null) {
            mapActionSubject.onNext(MapAction.MoveCameraToPosition(selectedStop.latLng))
        }
    }
}