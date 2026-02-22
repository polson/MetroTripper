package com.philsoft.metrotripper.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.MapStyleOptions
import com.philsoft.metrotripper.R
import com.philsoft.metrotripper.app.about.AboutDialog
import com.philsoft.metrotripper.app.ui.helper.LocationHelper
import com.philsoft.metrotripper.app.ui.slidingpanel.SlidingPanel
import com.philsoft.metrotripper.app.ui.view.MapHelper
import com.philsoft.metrotripper.app.ui.view.MapVehicleHelper
import com.philsoft.metrotripper.database.DatabasePopulator
import com.philsoft.metrotripper.databinding.ActivityMainBinding
import com.philsoft.metrotripper.prefs.Prefs
import com.philsoft.metrotripper.utils.EZ
import com.philsoft.metrotripper.utils.ui.Ui
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope

import com.philsoft.metrotripper.viewmodel.MainViewModel
import com.philsoft.metrotripper.viewmodel.MainViewModelFactory
import com.philsoft.metrotripper.viewmodel.MainUiState
import com.philsoft.metrotripper.viewmodel.MainUiEffect
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import timber.log.Timber

class MainActivity : BaseActivity(), OnMapReadyCallback {

    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this, MainViewModelFactory(this))[MainViewModel::class.java]
    }
    private val drawerToggle by lazy { MtDrawerToggle() }
    private lateinit var mapHelper: MapHelper
    private lateinit var mapVehicleHelper: MapVehicleHelper
    private val permissionGranted = MutableStateFlow(false)
    private val locationHelper by lazy { LocationHelper(this, permissionGranted) }
    private lateinit var binding: ActivityMainBinding

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        permissionGranted.value = isGranted
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setSupportActionBar(binding.toolbar)
        
        setupActionBar()
        populateStops()
        setupDrawer()
        setupMapFragment()
        setupSlidingPanel()
        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            permissionGranted.value = true
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun setupActionBar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(true)
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

    private fun setupDrawer() {
        binding.drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
    }

    private fun setupMapFragment() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun setupSlidingPanel() {
        binding.slidingPanel.setPanelState(SlidingPanel.PanelState.HIDDEN)
    }

    override fun onMapReady(map: GoogleMap) {
        setupMap(map)
        setupMapViewHelper(map)
        collectViewModelState()
        collectViewModelEffects()
        setupListeners()
    }

    private fun collectViewModelState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    render(state)
                }
            }
        }
    }

    private fun collectViewModelEffects() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiEffect.collect { effect ->
                    handleEffect(effect)
                }
            }
        }
    }

    private fun render(state: MainUiState) {
        // Map markers
        mapHelper.showStopMarkers(state.visibleStops)
        
        // Selected stop
        if (state.selectedStop != null) {
            binding.stopHeading.showStop(state.selectedStop, state.isSelectedStopSaved)
            mapVehicleHelper.displayVehicleMarkers(state.trips)
            binding.tripList.showTrips(state.trips)
        }
        
        // Trips loading
        if (state.isLoadingTrips) {
            binding.stopHeading.showProgressSpinner()
        } else {
            binding.stopHeading.hideProgressSpinner()
        }
        
        // Saved stops list in drawer
        binding.stopList.showStops(state.savedStops.values.toList())
        
        // Enable/disable location button based on state
        mapHelper.enableLocationButton(state.isLocationButtonEnabled)
    }

    private fun handleEffect(effect: MainUiEffect) {
        when (effect) {
            is MainUiEffect.MoveCamera -> mapHelper.moveCameraToPosition(effect.latLng)
            is MainUiEffect.SelectMarker -> mapHelper.selectStopMarker(effect.stop)
            is MainUiEffect.ShowError -> EZ.showSnackbar(binding.root, effect.message)
            is MainUiEffect.CloseDrawer -> binding.drawerLayout.closeDrawer()
            is MainUiEffect.ExpandPanel -> binding.slidingPanel.expand()
            is MainUiEffect.CollapsePanel -> binding.slidingPanel.collapse()
            is MainUiEffect.HidePanel -> binding.slidingPanel.hide()
        }
    }

    private fun setupListeners() {
        // Stop searched
        binding.stopList.stopSearchedEvent
            .onEach { viewModel.onStopSearched(it) }
            .launchIn(lifecycleScope)

        // Stop selected from drawer
        binding.stopList.stopSelectedEvent
            .onEach { viewModel.onStopSelectedFromDrawer(it) }
            .launchIn(lifecycleScope)

        // Marker clicked
        mapHelper.markerClicks
            .onEach { viewModel.onMarkerClicked(it) }
            .launchIn(lifecycleScope)

        // Camera idle
        mapHelper.cameraIdleEvents
            .onEach { viewModel.onCameraIdle(it) }
            .launchIn(lifecycleScope)

        // Location updates
        locationHelper.locationEvents
            .onEach { viewModel.onLocationUpdate(it) }
            .launchIn(lifecycleScope)

        // Button clicks
        binding.stopHeading.saveStopButtonClicks
            .onEach { viewModel.onSaveStopClicked() }
            .launchIn(lifecycleScope)

        binding.stopHeading.locationButtonClicks
            .onEach { viewModel.onLocationButtonClicked() }
            .launchIn(lifecycleScope)

        binding.stopHeading.scheduleButtonClicks
            .onEach { viewModel.onScheduleButtonClicked() }
            .launchIn(lifecycleScope)

        binding.slidingPanel.slidingPanelEvents
            .onEach { viewModel.onPanelExpanded() }
            .launchIn(lifecycleScope)
    }

    @SuppressLint("MissingPermission")
    private fun setupMap(map: GoogleMap) {
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
    }

    private fun setupMapViewHelper(map: GoogleMap) {
        val stopBitmap = Ui.createBitmapFromDrawableResource(this, 0, 0, R.drawable.ic_bus_stop_2)
        val starredBitmap = Ui.createBitmapFromDrawableResource(this, 0, 0, R.drawable.ic_star_yellow)
        mapHelper = MapHelper(stopBitmap, starredBitmap, map)
        mapVehicleHelper = MapVehicleHelper(this, map)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_activity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        return when (item.itemId) {
            R.id.about -> {
                AboutDialog.show(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private inner class MtDrawerToggle : ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, R.string.drawer_open, R.string.drawer_close) {
        override fun onDrawerClosed(drawerView: View) {
            super.onDrawerClosed(drawerView)
            viewModel.onDrawerClosed()
            EZ.hideKeyboard(this@MainActivity)
        }

        override fun onDrawerOpened(drawerView: View) {
            super.onDrawerOpened(drawerView)
            viewModel.onDrawerOpened()
        }
    }
}

