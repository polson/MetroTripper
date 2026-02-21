package com.philsoft.metrotripper.activity

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.support.v7.app.ActionBarDrawerToggle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
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
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.merge

class MainActivity : BaseActivity(), OnMapReadyCallback {

    private val nexTripApiHelper = NexTripApiHelper()
    private val drawerToggle by lazy { MtDrawerToggle() }
    private lateinit var mapHelper: MapHelper
    private lateinit var mapVehicleHelper: MapVehicleHelper
    private val rxPermissions = RxPermissions(this)
    private val locationHelper by lazy { LocationHelper(this, rxPermissions) }
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupActionBar()
        populateStops()
        setupDrawer()
        setupMapFragment()
        setupSlidingPanel()
        locationHelper
    }

    private fun setupActionBar() {
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeButtonEnabled(true)
        actionBar.setDisplayShowTitleEnabled(false)
    }

    private fun populateStops() {
        io.reactivex.Completable.fromAction {
            val populator = DatabasePopulator(this)
            if (populator.isTableEmpty()) {
                populator.populateStopsFast()
            }
        }.subscribeOn(io.reactivex.schedulers.Schedulers.io())
         .observeOn(AndroidSchedulers.mainThread())
         .subscribe({
             // Done
         }, { t ->
             timber.log.Timber.e(t, "Error populating stops")
         })
    }

    private fun setupDrawer() {
        binding.drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
    }

    private fun setupMapFragment() {
        val mapFragment = fragmentManager.findFragmentById(R.id.map) as MapFragment
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
        uiEvents.observeOn(io.reactivex.schedulers.Schedulers.io())
                .compose(appStateTransformer)
                .compose(appActionTransformer)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { appAction ->
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

    private fun buildUiEvents(): Observable<AppUiEvent> {
        return listOf(
                binding.stopHeading.scheduleButtonClicks,
                binding.stopHeading.locationButtonClicks,
                binding.stopHeading.saveStopButtonClicks,
                binding.stopList.stopSearchedEvent,
                binding.stopList.stopSelectedEvent,
                mapHelper.cameraIdleEvents,
                mapHelper.markerClicks,
                locationHelper.locationEvents,
                binding.slidingPanel.slidingPanelEvents,
                nexTripApiHelper.apiResultEvents,
                Observable.just(AppUiEvent.Initialize).share()
        ).merge()
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

