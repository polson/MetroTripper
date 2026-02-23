package com.philsoft.metrotripper.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.MapStyleOptions
import com.philsoft.metrotripper.R
import com.philsoft.metrotripper.app.about.AboutDialog
import com.philsoft.metrotripper.app.ui.helper.LocationHelper
import com.philsoft.metrotripper.app.ui.view.MainScreen
import com.philsoft.metrotripper.app.ui.view.MapHelper
import com.philsoft.metrotripper.app.ui.view.MapVehicleHelper
import com.philsoft.metrotripper.database.DatabasePopulator
import com.philsoft.metrotripper.databinding.ActivityMainBinding
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

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            permissionGranted.value = isGranted
        }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        setupActionBar()
        populateStops()
        setupDrawer()
        checkLocationPermission()

        binding.mainComposeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MainScreen(
                    viewModel = viewModel,
                    supportFragmentManager = supportFragmentManager,
                    mapReadyCallback = this@MainActivity,
                    onEffect = ::handleEffect
                )
            }
        }

        // Listen to stop selections from the drawer
        lifecycleScope.launch {
            binding.stopList.stopSearchedEvent.collect { viewModel.onStopSearched(it) }
        }
        lifecycleScope.launch {
            binding.stopList.stopSelectedEvent.collect { viewModel.onStopSelectedFromDrawer(it) }
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

    override fun onMapReady(map: GoogleMap) {
        setupMap(map)
        setupMapViewHelper(map)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // Map markers
                    mapHelper.showStopMarkers(state.visibleStops)

                    if (state.selectedStop != null) {
                        mapVehicleHelper.displayVehicleMarkers(state.trips)
                    }

                    // Saved stops list in drawer
                    binding.stopList.showStops(state.savedStops.values.toList())

                    // Enable/disable location button based on state
                    mapHelper.enableLocationButton(state.isLocationButtonEnabled)
                }
            }
        }
        setupMapListeners()
    }

    private fun setupMapListeners() {
        // Marker clicked
        lifecycleScope.launch {
            mapHelper.markerClicks.collect { viewModel.onMarkerClicked(it) }
        }
        // Camera idle
        lifecycleScope.launch {
            mapHelper.cameraIdleEvents.collect { viewModel.onCameraIdle(it) }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    private fun handleEffect(
        effect: MainUiEffect,
        scaffoldState: BottomSheetScaffoldState,
        scope: CoroutineScope
    ) {
        when (effect) {
            is MainUiEffect.MoveCamera -> mapHelper.moveCameraToPosition(effect.latLng)
            is MainUiEffect.SelectMarker -> mapHelper.selectStopMarker(effect.stop)
            is MainUiEffect.ShowError -> EZ.showSnackbar(binding.root, effect.message)
            is MainUiEffect.CloseDrawer -> binding.drawerLayout.closeDrawer()
            is MainUiEffect.ExpandPanel -> scope.launch { scaffoldState.bottomSheetState.expand() }
            is MainUiEffect.CollapsePanel -> scope.launch { scaffoldState.bottomSheetState.partialExpand() }
            is MainUiEffect.HidePanel -> scope.launch { scaffoldState.bottomSheetState.hide() }
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

    private inner class MtDrawerToggle : ActionBarDrawerToggle(
        this,
        binding.drawerLayout,
        binding.toolbar,
        R.string.drawer_open,
        R.string.drawer_close
    ) {
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

