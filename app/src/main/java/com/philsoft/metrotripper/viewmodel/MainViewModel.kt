package com.philsoft.metrotripper.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.philsoft.metrotripper.model.Stop
import com.philsoft.metrotripper.repository.NexTripRepository
import com.philsoft.metrotripper.repository.StopsRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class MainViewModel(
    private val stopsRepository: StopsRepository,
    private val nexTripRepository: NexTripRepository
) : ViewModel() {

    companion object {
        private const val MAX_STOPS = 20
        const val MIN_ZOOM_LEVEL = 16f
        private val MINNEAPOLIS_LATLNG = LatLng(44.9799700, -93.2638400)
    }

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<MainUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    init {
        loadInitialState()
    }

    private fun loadInitialState() {
        viewModelScope.launch {
            val savedStopIds = stopsRepository.getSavedStopIds()
            val savedStopsList = stopsRepository.getStopsById(savedStopIds)
            val savedStopsMap = savedStopsList.associateBy { it.stopId }
            _uiState.update { it.copy(savedStops = savedStopsMap) }
        }
    }

    fun onStopSearched(stopId: Long) {
        viewModelScope.launch {
            val stop = stopsRepository.getStopById(stopId)
            if (stop != null) {
                handleStopSelected(stop)
            } else {
                _uiEffect.emit(MainUiEffect.ShowError("Stop not found"))
            }
        }
    }

    fun onMarkerClicked(stopId: Long) {
        viewModelScope.launch {
            val stop = stopsRepository.getStopById(stopId)
            if (stop != null) {
                handleStopSelected(stop)
            }
        }
    }

    fun onCameraIdle(cameraPosition: CameraPosition) {
        viewModelScope.launch {
            val visibleStops = if (cameraPosition.zoom < MIN_ZOOM_LEVEL) {
                emptyList()
            } else {
                stopsRepository.getClosestStops(
                    cameraPosition.target.latitude,
                    cameraPosition.target.longitude,
                    MAX_STOPS
                )
            }
            _uiState.update { it.copy(visibleStops = visibleStops) }
        }
    }

    fun onSaveStopClicked() {
        val selectedStop = _uiState.value.selectedStop ?: return
        val isSaved = _uiState.value.isSelectedStopSaved
        
        if (isSaved) {
            stopsRepository.unsaveStop(selectedStop.stopId)
            _uiState.update { state ->
                state.copy(
                    isSelectedStopSaved = false,
                    savedStops = state.savedStops.filterKeys { it != selectedStop.stopId }
                )
            }
        } else {
            stopsRepository.saveStop(selectedStop.stopId)
            _uiState.update { state ->
                state.copy(
                    isSelectedStopSaved = true,
                    savedStops = state.savedStops + (selectedStop.stopId to selectedStop)
                )
            }
        }
    }

    fun onStopSelectedFromDrawer(stop: Stop) {
        viewModelScope.launch {
            handleStopSelected(stop)
            _uiState.update { it.copy(isDrawerOpen = false) }
            _uiEffect.emit(MainUiEffect.CloseDrawer)
        }
    }

    fun onDrawerOpened() {
        _uiState.update { it.copy(isDrawerOpen = true) }
    }

    fun onDrawerClosed() {
        _uiState.update { it.copy(isDrawerOpen = false) }
    }

    fun onLocationUpdate(locationResult: LocationResult) {
        viewModelScope.launch {
            val lastLocation = locationResult.lastLocation
            if (lastLocation != null) {
                val latLng = LatLng(lastLocation.latitude, lastLocation.longitude)
                _uiState.update { it.copy(isLocationButtonEnabled = true) }
                _uiEffect.emit(MainUiEffect.MoveCamera(latLng))
            } else {
                _uiEffect.emit(MainUiEffect.MoveCamera(MINNEAPOLIS_LATLNG))
            }
        }
    }

    fun onLocationButtonClicked() {
        val selectedStop = _uiState.value.selectedStop
        if (selectedStop != null) {
            viewModelScope.launch {
                _uiEffect.emit(MainUiEffect.MoveCamera(selectedStop.latLng))
                _uiEffect.emit(MainUiEffect.SelectMarker(selectedStop))
                _uiEffect.emit(MainUiEffect.CollapsePanel)
            }
        }
    }

    fun onPanelExpanded() {
        val selectedStop = _uiState.value.selectedStop ?: return
        loadTrips(selectedStop.stopId, expandPanel = false)
    }

    fun onScheduleButtonClicked() {
        val selectedStop = _uiState.value.selectedStop ?: return
        loadTrips(selectedStop.stopId, expandPanel = true)
    }

    private fun loadTrips(stopId: Long, expandPanel: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingTrips = true) }
            if (expandPanel) {
                _uiEffect.emit(MainUiEffect.ExpandPanel)
            }
            
            val result = nexTripRepository.getTrips(stopId)
            result.onSuccess { trips ->
                _uiState.update { it.copy(isLoadingTrips = false, trips = trips) }
            }.onFailure { e ->
                Timber.e(e, "Error loading trips")
                _uiState.update { it.copy(isLoadingTrips = false) }
                _uiEffect.emit(MainUiEffect.ShowError("Error loading trips"))
            }
        }
    }

    private suspend fun handleStopSelected(stop: Stop) {
        val isSaved = stopsRepository.isStopSaved(stop.stopId)
        _uiState.update { it.copy(selectedStop = stop, isSelectedStopSaved = isSaved) }
        _uiEffect.emit(MainUiEffect.MoveCamera(stop.latLng))
        _uiEffect.emit(MainUiEffect.SelectMarker(stop))
        loadTrips(stop.stopId, expandPanel = false)
    }
}
