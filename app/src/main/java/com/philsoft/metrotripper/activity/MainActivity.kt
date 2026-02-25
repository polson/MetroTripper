package com.philsoft.metrotripper.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.core.content.ContextCompat
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
import com.philsoft.metrotripper.database.DatabasePopulator
import com.philsoft.metrotripper.database.dao.StopDao
import com.philsoft.metrotripper.prefs.Prefs
import com.philsoft.metrotripper.utils.EZ
import com.philsoft.metrotripper.utils.ui.Ui
import com.philsoft.metrotripper.viewmodel.MainUiEffect
import com.philsoft.metrotripper.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : BaseActivity(), OnMapReadyCallback {

    private val viewModel: MainViewModel by viewModel()
    private val prefs: Prefs by inject()
    private val stopDao: StopDao by inject()
    
    private lateinit var mapHelper: MapHelper
    private lateinit var mapVehicleHelper: MapVehicleHelper
    private val permissionGranted = MutableStateFlow(false)
    private val locationHelper by lazy { LocationHelper(this, permissionGranted) }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val fine = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
            val coarse = result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            permissionGranted.value = fine || coarse
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT,
                Color.TRANSPARENT
            )
        )
        populateStops()
        checkLocationPermission()

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

    private fun checkLocationPermission() {
        val fineGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            permissionGranted.value = true
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

    private fun populateStops() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val currentVersion = DatabasePopulator.CURRENT_VERSION
                val stopsVersion = prefs.getStopsVersion()
                val populator = DatabasePopulator(stopDao)
                if (populator.isTableEmpty || stopsVersion < currentVersion) {
                    Timber.d("Populating stops... current version: $stopsVersion, target version: $currentVersion")
                    val stopData = assets.open("stops.txt")
                    stopData.use { inputStream ->
                        populator.populateStops(inputStream)
                    }
                    prefs.setStopsVersion(currentVersion)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error populating stops")
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

