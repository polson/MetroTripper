package com.philsoft.metrotripper.app.state

import android.content.Context
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.philsoft.metrotripper.app.nextrip.NexTripService
import com.philsoft.metrotripper.database.DataProvider
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class AppStateManager(val context: Context) {
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

    //State
    private var state = AppState(
            selectedStop = null
    )

    fun handleEvent(appEvent: AppEvent) {
        val something = when (appEvent) {
            AppEvent.ShowStops -> showStops()
            AppEvent.ShowCurrentStopLocation -> showCurrentStopLocation()
            is AppEvent.ShowSchedule -> showSchedule()
            is AppEvent.SearchStop -> searchStop(appEvent.stopId)
            is AppEvent.CameraIdle -> handleCameraIdle(appEvent.cameraPosition)
            is AppEvent.InitialLocationUpdate -> handleInitialLocationUpdate(appEvent.locationResult)
            is AppEvent.MarkerClick -> handleMarkerClick(appEvent.marker)
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
            mapActionSubject.onNext(MapAction.MoveCameraToPosition(latLng))
        } else {
            mapActionSubject.onNext(MapAction.MoveCameraToPosition(MINNEAPOLIS_LATLNG))
        }
    }

    private fun handleCameraIdle(position: CameraPosition) {
        val stops = dataProvider.getClosestStops(position.target.latitude, position.target.longitude, MAX_STOPS)
        mapActionSubject.onNext(MapAction.ShowStopMarkers(stops))
    }

    private fun searchStop(stopId: Long) {
        val stop = dataProvider.getStopById(stopId)
        if (stop != null) {
            state = state.copy(selectedStop = stop)
            stopHeadingActionSubject.onNext(StopHeadingAction.ShowStop(stop, false))
            mapActionSubject.onNext(MapAction.MoveCameraToPosition(stop.latLng))
            showSchedule()
        }
    }

    private fun showStops() {

    }

    private fun showSchedule() {
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


    private fun showCurrentStopLocation() {
        val selectedStop = state.selectedStop
        if (selectedStop != null) {
            mapActionSubject.onNext(MapAction.MoveCameraToPosition(selectedStop.latLng))
        }
    }
}