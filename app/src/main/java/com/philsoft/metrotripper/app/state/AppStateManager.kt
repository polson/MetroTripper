package com.philsoft.metrotripper.app.state

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.philsoft.metrotripper.app.SettingsProvider
import com.philsoft.metrotripper.app.nextrip.NexTripService
import com.philsoft.metrotripper.database.DataProvider
import com.philsoft.metrotripper.model.Stop
import com.philsoft.metrotripper.prefs.Prefs
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class AppStateManager(context: Context, appUiEvents: Observable<AppUiEvent>) {

    companion object {
        private val MAX_STOPS = 20
        private val MINNEAPOLIS_LATLNG = LatLng(44.9799700, -93.2638400)
    }

    //Services
    private val nexTripService = NexTripService.create()
    private val dataProvider = DataProvider(context)
    private val settingsProvider = SettingsProvider(Prefs.getInstance(context))

    private val scanner = appUiEvents.scan(AppState(null), { previousState, appEvent ->
        when (appEvent) {
            is AppUiEvent.StopSearched -> {
                val stop = dataProvider.getStopById(appEvent.stopId)
                previousState.copy(selectedStop = stop)
            }
            else -> {
                previousState
            }
        }
    }).doOnNext { state -> Timber.d("State: $state") }

    //State Transformers
    private val stateTransformer = ObservableTransformer<AppUiEvent, AppState> { observable ->
        observable.
                withLatestFrom(scanner)
                .map { pair -> pair.second }
    }

    private val stateSelectedStopTransformer = ObservableTransformer<AppUiEvent, Stop> { observable ->
        observable
                .compose(stateTransformer)
                .filter { state -> state.selectedStop != null }
                .map { state -> state.selectedStop }
    }

    //Action transformers
    val locationUpdateTransformer = ObservableTransformer<AppUiEvent.InitialLocationUpdate, AppAction> { observable ->
        observable
                .map { event -> event.locationResult }
                .map { locationResult ->
                    val lastLocation = locationResult.lastLocation
                    if (locationResult.lastLocation != null) {
                        val latLng = LatLng(lastLocation.latitude, lastLocation.longitude)
                        MapAction.MoveCameraToPosition(latLng)
                    } else {
                        MapAction.MoveCameraToPosition(MINNEAPOLIS_LATLNG)
                    }
                }
    }

    val toggleCurrenStopSavedTransformer = ObservableTransformer<AppUiEvent.SaveStopButtonClicked, AppAction> { observable ->
        observable
                .compose(stateSelectedStopTransformer)
                .map { stop ->
                    val stopId = stop.stopId
                    if (settingsProvider.isStopSaved(stopId)) {
                        settingsProvider.unsaveStop(stopId)
                        StopHeadingAction.UnsaveStop
                    } else {
                        settingsProvider.saveStop(stopId)
                        StopHeadingAction.SaveStop
                    }
                }

    }

    val cameraIdleTransformer = ObservableTransformer<AppUiEvent.CameraIdle, AppAction> { observable ->
        observable
                .map { event ->
                    val position = event.cameraPosition
                    val stops = dataProvider.getClosestStops(position.target.latitude, position.target.longitude, MAX_STOPS)
                    val savedStopIds = settingsProvider.getSavedStopIds()
                    MapAction.ShowStopMarkers(stops, savedStopIds)
                }
    }

    val showScheduleTransformer = ObservableTransformer<AppUiEvent.ScheduleButtonClicked, AppAction> { observable ->
        observable
                .compose(stateSelectedStopTransformer)
                .flatMap { stop ->
                    val startAction = Observable.just(StopHeadingAction.LoadingTrips)
                    Observable.merge(startAction, getTrips(stop.stopId))
                }

    }

    val markerClickTransformer = ObservableTransformer<AppUiEvent.MarkerClicked, AppAction> { observable ->
        observable
                .flatMap { event ->
                    val stopId = event.marker.title.toLong()
                    getTrips(stopId)
                }
    }

    val showCurrentStopTransformer = ObservableTransformer<AppUiEvent.LocationButtonClicked, AppAction> { observable ->
        observable
                .compose(stateSelectedStopTransformer)
                .map { stop ->
                    MapAction.MoveCameraToPosition(stop.latLng)
                }
    }

    val searchStopTransformer = ObservableTransformer<AppUiEvent.StopSearched, AppAction> { observable ->
        observable
                .withLatestFrom(scanner)
                .filter { pair ->
                    val (event, state) = pair
                    state.selectedStop != null
                }
                .flatMap { pair ->
                    val (event, state) = pair
                    val stop = state.selectedStop
                    val isSavedStop = settingsProvider.isStopSaved(event.stopId)

                    val startEvents = Observable.fromArray(
                            DrawerAction.CloseDrawer,
                            StopHeadingAction.ShowStop(stop!!, isSavedStop),
                            MapAction.MoveCameraToPosition(stop.latLng),
                            MapAction.SelectStopMarker(stop!!, isSavedStop))

                    Observable.merge(startEvents, getTrips(event.stopId))
                }
    }

    fun getTrips(stopId: Long): Observable<AppAction> {
        return nexTripService.getTrips(stopId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap<AppAction> { trips ->
                    Observable.fromArray(
                            TripListAction.ShowTrips(trips),
                            StopHeadingAction.LoadTripsComplete())
                }
                .onErrorReturn {
                    StopHeadingAction.LoadTripsError()
                }
    }


}