package com.philsoft.metrotripper.activity

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.MapStyleOptions
import com.philsoft.metrotripper.R
import com.philsoft.metrotripper.app.ui.helper.LocationHelper
import com.philsoft.metrotripper.app.ui.view.MapHelper
import com.philsoft.metrotripper.app.ui.view.MapVehicleHelper
import com.philsoft.metrotripper.app.ui.view.MetroTripperApp
import com.philsoft.metrotripper.utils.EZ
import com.philsoft.metrotripper.utils.ui.Ui
import com.philsoft.metrotripper.viewmodel.MainUiEffect
import com.philsoft.metrotripper.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : BaseActivity(), OnMapReadyCallback {

    private val viewModel: MainViewModel by viewModel()
    private lateinit var mapHelper: MapHelper
    private lateinit var mapVehicleHelper: MapVehicleHelper
    private val locationHelper by lazy { LocationHelper(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT,
                Color.TRANSPARENT
            )
        )
        locationHelper.checkLocationPermission()
        viewModel.initialize()

        setContent {
            MetroTripperApp(
                viewModel = viewModel,
                mapReadyCallback = this@MainActivity,
                onEffect = ::handleEffect
            )
        }

        lifecycleScope.launch {
            locationHelper.locationEvents.collect {
                viewModel.onLocationUpdate(it)
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        setupMap(map)
        setupMapViewHelper(map)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    mapHelper.showStopMarkers(state.visibleStops)
                    mapHelper.enableLocationButton(state.isLocationButtonEnabled)

                    if (state.selectedStop != null) {
                        mapVehicleHelper.displayVehicleMarkers(state.trips)
                    }
                }
            }
        }
        setupMapListeners()
    }

    private fun setupMapListeners() {
        lifecycleScope.launch {
            mapHelper.markerClicks.collect { viewModel.onMarkerClicked(it) }
        }
        lifecycleScope.launch {
            mapHelper.cameraIdleEvents.collect { viewModel.onCameraIdle(it) }
        }
    }

    private fun handleEffect(effect: MainUiEffect) {
        when (effect) {
            is MainUiEffect.MoveCamera -> mapHelper.moveCameraToPosition(effect.latLng)
            is MainUiEffect.SelectMarker -> mapHelper.selectStopMarker(effect.stop)
            is MainUiEffect.ShowError -> EZ.showSnackbar(findViewById(android.R.id.content), effect.message)
            else -> {}
        }
    }

    @SuppressLint("MissingPermission")
    private fun setupMap(map: GoogleMap) {
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
    }

    private fun setupMapViewHelper(map: GoogleMap) {
        val stopBitmap = Ui.createBitmapFromDrawableResource(this, 0, 0, R.drawable.ic_bus_stop_2)
        val starredBitmap =
            Ui.createBitmapFromDrawableResource(this, 0, 0, R.drawable.ic_star_yellow)
        mapHelper = MapHelper(stopBitmap, starredBitmap, map)
        mapVehicleHelper = MapVehicleHelper(this, map)
    }
}

