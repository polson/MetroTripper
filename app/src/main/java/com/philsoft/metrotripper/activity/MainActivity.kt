package com.philsoft.metrotripper.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
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
import com.philsoft.metrotripper.prefs.Prefs
import com.philsoft.metrotripper.utils.EZ
import com.philsoft.metrotripper.utils.ui.Ui
import com.philsoft.metrotripper.viewmodel.MainUiEffect
import com.philsoft.metrotripper.viewmodel.MainViewModel
import com.philsoft.metrotripper.viewmodel.MainViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : BaseActivity(), OnMapReadyCallback {

    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this, MainViewModelFactory(this))[MainViewModel::class.java]
    }
    
    private lateinit var mapHelper: MapHelper
    private lateinit var mapVehicleHelper: MapVehicleHelper
    private val permissionGranted = MutableStateFlow(false)
    private val locationHelper by lazy { LocationHelper(this, permissionGranted) }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            permissionGranted.value = isGranted
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
            locationHelper.locationEvents.collect { viewModel.onLocationUpdate(it) }
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            permissionGranted.value = true
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun populateStops() {
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val populator = DatabasePopulator(this@MainActivity)
                    val prefs = Prefs.getInstance(this@MainActivity)
                    val currentVersion = 2 // Update this version when stops.txt is updated
                    if (populator.isTableEmpty() || prefs.stopsVersion < currentVersion) {
                        Timber.d("Populating stops... current version: ${prefs.stopsVersion}, target version: $currentVersion")
                        populator.clearStops()
                        populator.populateStopsFast()
                        prefs.stopsVersion = currentVersion
                    }
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

    private fun handleEffect(
        effect: MainUiEffect,
        scaffoldState: BottomSheetScaffoldState,
        scope: CoroutineScope
    ) {
        when (effect) {
            is MainUiEffect.MoveCamera -> mapHelper.moveCameraToPosition(effect.latLng)
            is MainUiEffect.SelectMarker -> mapHelper.selectStopMarker(effect.stop)
            is MainUiEffect.ShowError -> EZ.showSnackbar(findViewById(android.R.id.content), effect.message)
            is MainUiEffect.ExpandPanel -> scope.launch { scaffoldState.bottomSheetState.expand() }
            is MainUiEffect.CollapsePanel -> scope.launch { scaffoldState.bottomSheetState.partialExpand() }
            is MainUiEffect.HidePanel -> scope.launch { scaffoldState.bottomSheetState.hide() }
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

