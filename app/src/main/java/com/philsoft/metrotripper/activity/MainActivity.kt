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
import com.philsoft.metrotripper.app.SettingsProvider
import com.philsoft.metrotripper.app.about.AboutDialog
import com.philsoft.metrotripper.app.state.*
import com.philsoft.metrotripper.app.ui.helper.LocationHelper
import com.philsoft.metrotripper.app.ui.helper.NexTripApiHelper
import com.philsoft.metrotripper.app.ui.slidingpanel.SlidingPanel
import com.philsoft.metrotripper.app.ui.view.MapHelper
import com.philsoft.metrotripper.app.ui.view.MapVehicleHelper
import com.philsoft.metrotripper.database.DataProvider
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

class MainActivity : BaseActivity(), OnMapReadyCallback {

    private val nexTripApiHelper by lazy { NexTripApiHelper(lifecycleScope) }
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
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun populateStops() {
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val populator = DatabasePopulator(this@MainActivity)
                    if (populator.isTableEmpty()) {
                        populator.populateStopsFast()
                    }
                }
            } catch (e: Exception) {
                timber.log.Timber.e(e, "Error populating stops")
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
        setupEventLoop()
    }

    @SuppressLint("MissingPermission")
    private fun setupMap(map: GoogleMap) {
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
    }

    private fun setupMapViewHelper(map: GoogleMap) {
        val stopBitmap = Ui.createBitmapFromDrawableResource(this, -30, -30, R.drawable.ic_bus_stop)
        val starredBitmap = Ui.createBitmapFromLayoutResource(this, R.layout.starred_stop)
        mapHelper = MapHelper(stopBitmap, starredBitmap, map)
        mapVehicleHelper = MapVehicleHelper(this, map)
    }

    private fun setupEventLoop() {
        val prefs = Prefs.getInstance(this)
        val settingsProvider = SettingsProvider(prefs)
        val dataProvider = DataProvider(this)
        val appStateTransformer = AppStateTransformer(dataProvider, settingsProvider)
        val appActionTransformer = AppActionTransformer()
        val uiEvents = buildUiEvents()
        
        lifecycleScope.launch {
            appActionTransformer.transform(
                appStateTransformer.transform(uiEvents).flowOn(Dispatchers.IO)
            ).collect { appAction ->
                when (appAction) {
                    is MapAction -> mapHelper.render(appAction)
                    is StopHeadingAction -> binding.stopHeading.render(appAction)
                    is TripListAction -> binding.tripList.render(appAction)
                    is NexTripAction -> nexTripApiHelper.render(appAction)
                    is DrawerAction -> binding.drawerLayout.render(appAction)
                    is SlidingPanelAction -> binding.slidingPanel.render(appAction)
                    is StopListAction -> binding.stopList.render(appAction)
                    is VehicleAction -> mapVehicleHelper.render(appAction)
                }
            }
        }
    }

    private fun buildUiEvents(): Flow<AppUiEvent> {
        return merge(
                binding.stopHeading.scheduleButtonClicks,
                binding.stopHeading.locationButtonClicks,
                binding.stopHeading.saveStopButtonClicks,
                binding.stopList.stopSearchedEvent,
                binding.stopList.stopSelectedEvent,
                mapHelper.cameraIdleEvents,
                mapHelper.markerClicks,
                locationHelper.locationEvents,
                binding.slidingPanel.slidingPanelEvents,
                nexTripApiHelper.apiResultEvents
        )
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_activity, menu)
        return super.onCreateOptionsMenu(menu)
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

    private inner class MtDrawerToggle : ActionBarDrawerToggle(this, binding.drawerLayout, R.string.drawer_open, R.string.drawer_close) {
        override fun onDrawerClosed(drawerView: View) {
            super.onDrawerClosed(drawerView)
            EZ.hideKeyboard(this@MainActivity)
        }
    }
}

