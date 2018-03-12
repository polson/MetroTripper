package com.philsoft.metrotripper.app.state

import android.content.Context
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.philsoft.metrotripper.app.SettingsProvider
import com.philsoft.metrotripper.app.nextrip.NexTripService
import com.philsoft.metrotripper.database.DataProvider
import com.philsoft.metrotripper.model.Stop
import com.philsoft.metrotripper.prefs.Prefs
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class AppStateManager(context: Context) {
    companion object {
        private val MAX_STOPS = 20
        private val MINNEAPOLIS_LATLNG = LatLng(44.9799700, -93.2638400)
    }

    //Subjects
    private val mapActionSubject = PublishSubject.create<MapAction>()
    private val stopHeadingActionSubject = PublishSubject.create<StopHeadingAction>()
    private val tripListActionSubject = PublishSubject.create<TripListAction>()

    //Outputs
    val mapActions: Observable<MapAction> = mapActionSubject.hide()
    val stopHeadingActions: Observable<StopHeadingAction> = stopHeadingActionSubject.hide()
    val tripListActions: Observable<TripListAction> = tripListActionSubject.hide()

    //Services
    private val nexTripService = NexTripService.create()
    private val dataProvider = DataProvider(context)
    private val settingsProvider = SettingsProvider(Prefs.getInstance(context))

    //State
    private var state = AppState(
            selectedStop = null
    )

    fun handleEvent(appEvent: AppEvent) {
        val something = when (appEvent) {
            AppEvent.ShowStops -> showStops()
            AppEvent.SaveStop -> toggleCurrentStopSaved()
            AppEvent.ShowCurrentStopLocation -> showCurrentStopLocation()
            is AppEvent.ShowSchedule -> showSchedule(state.selectedStop)

            is AppEvent.SearchStop -> searchStop(appEvent.stopId)
            is AppEvent.CameraIdle -> handleCameraIdle(appEvent.cameraPosition)
            is AppEvent.InitialLocationUpdate -> handleInitialLocationUpdate(appEvent.locationResult)
            is AppEvent.MarkerClick -> handleMarkerClick(appEvent.marker)
        }
    }

    private fun toggleCurrentStopSaved() {
        if (state.hasSelectedStop()) {
            val stopId = state.selectedStop!!.stopId
            if (settingsProvider.isStopSaved(stopId)) {
                settingsProvider.unsaveStop(stopId)
                StopHeadingAction.UnsaveStop.send()
            } else {
                settingsProvider.saveStop(stopId)
                StopHeadingAction.SaveStop.send()
            }
        }
    }

    private fun handleMarkerClick(marker: Marker) {
        val stopId = marker.title.toLong()
        searchStop(stopId)
    }

    private fun handleInitialLocationUpdate(locationResult: LocationResult) {
        val lastLocation = locationResult.lastLocation
        if (locationResult.lastLocation != null) {
            val latLng = LatLng(lastLocation.latitude, lastLocation.longitude)
            MapAction.MoveCameraToPosition(latLng).send()
        } else {
            MapAction.MoveCameraToPosition(MINNEAPOLIS_LATLNG).send()
        }
    }

    private fun handleCameraIdle(position: CameraPosition) {
        val stops = dataProvider.getClosestStops(position.target.latitude, position.target.longitude, MAX_STOPS)
        val savedStopIds = settingsProvider.getSavedStopIds()
        MapAction.ShowStopMarkers(stops, savedStopIds).send()
    }

    private fun searchStop(stopId: Long) {
        val stop = dataProvider.getStopById(stopId)
        if (stop != null) {
            state = state.copy(selectedStop = stop)
            val isSavedStop = settingsProvider.isStopSaved(stop.stopId)
            StopHeadingAction.ShowStop(stop, isSavedStop).send()
            MapAction.MoveCameraToPosition(stop.latLng).send()
            MapAction.SelectStopMarker(stop, isSavedStop).send()
            showSchedule(stop)
        }
    }

    private fun showStops() {

    }

    private fun showSchedule(stop: Stop?) {
        if (stop == null) {
            return //TODO: gross
        }
        val selectedStop = state.selectedStop
        if (selectedStop != null) {
            StopHeadingAction.LoadingTrips.send()
            nexTripService.getTrips(selectedStop.stopId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError { throwable ->
                        Timber.d("Unable to get trips: ${throwable.message}")
                        StopHeadingAction.LoadTripsError().send()
                    }
                    .subscribe { trips ->
                        StopHeadingAction.LoadTripsComplete().send()
                        TripListAction.ShowTrips(trips).send()
                    }
        }
    }


    private fun showCurrentStopLocation() {
        val selectedStop = state.selectedStop
        if (selectedStop != null) {
            MapAction.MoveCameraToPosition(selectedStop.latLng).send()
        }
    }

    //Extension functions
    private fun MapAction.send() {
        mapActionSubject.onNext(this)
    }

    private fun StopHeadingAction.send() {
        stopHeadingActionSubject.onNext(this)
    }

    private fun TripListAction.send() {
        tripListActionSubject.onNext(this)
    }

    private fun AppState.hasSelectedStop(): Boolean = selectedStop != null

}

