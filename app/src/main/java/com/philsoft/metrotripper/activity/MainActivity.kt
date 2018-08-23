package com.philsoft.metrotripper.activity

import android.content.res.Configuration
import android.os.Bundle
import android.support.v7.app.ActionBarDrawerToggle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.MapStyleOptions
import com.philsoft.metrotripper.R
import com.philsoft.metrotripper.app.SettingsProvider
import com.philsoft.metrotripper.app.about.AboutDialog
import com.philsoft.metrotripper.app.state.*
import com.philsoft.metrotripper.app.ui.slidingpanel.SlidingPanel
import com.philsoft.metrotripper.app.ui.view.MapHelper
import com.philsoft.metrotripper.app.ui.view.MapVehicleHelper
import com.philsoft.metrotripper.app.ui.view.NexTripApiHelper
import com.philsoft.metrotripper.database.DataProvider
import com.philsoft.metrotripper.database.DatabasePopulator
import com.philsoft.metrotripper.prefs.Prefs
import com.philsoft.metrotripper.utils.EZ
import com.philsoft.metrotripper.utils.map.RxLocation
import com.philsoft.metrotripper.utils.ui.Ui
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.merge
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit

class MainActivity : BaseActivity(), OnMapReadyCallback {

    private val nexTripApiHelper = NexTripApiHelper()
    private val drawerToggle by lazy { MtDrawerToggle() }
    private lateinit var mapHelper: MapHelper
    private lateinit var mapVehicleHelper: MapVehicleHelper

    private val locationEvents by lazy {
        val client = LocationServices.getFusedLocationProviderClient(this)
        RxLocation.locationEvents(client).map { locationResult ->
            LocationUiEvent.InitialLocationUpdate(locationResult)
        }.share()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupActionBar()
        populateStops()
        setupDrawer()
        setupMapFragment()
        setupSlidingPanel()
    }

    private fun setupActionBar() {
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeButtonEnabled(true)
        actionBar.setDisplayShowTitleEnabled(false)
    }

    private fun populateStops() = DatabasePopulator(this).apply {
        if (isTableEmpty()) {
            populateStopsFast()
        }
    }

    private fun setupDrawer() {
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
    }

    private fun setupMapFragment() {
        val mapFragment = fragmentManager.findFragmentById(R.id.map) as MapFragment
        mapFragment.getMapAsync(this)
    }

    private fun setupSlidingPanel() {
        slidingPanel.setPanelState(SlidingPanel.PanelState.HIDDEN)
    }

    override fun onMapReady(map: GoogleMap) {
        setupMap(map)
        setupMapViewHelper(map)
        setupEventLoop()
    }

    private fun setupMap(map: GoogleMap) {
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
        map.isMyLocationEnabled = true // show location button
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
        uiEvents.observeOn(AndroidSchedulers.mainThread())
                .compose(appStateTransformer)
                .compose(appActionTransformer)
                .subscribe { appAction ->
                    when (appAction) {
                        is MapAction -> mapHelper.render(appAction)
                        is StopHeadingAction -> stopHeading.render(appAction)
                        is TripListAction -> tripList.render(appAction)
                        is NexTripAction -> nexTripApiHelper.render(appAction)
                        is DrawerAction -> drawerLayout.render(appAction)
                        is SlidingPanelAction -> slidingPanel.render(appAction)
                        is StopListAction -> stopList.render(appAction)
                        is VehicleAction -> mapVehicleHelper.render(appAction)
                    }
                }
    }

    private fun buildUiEvents(): Observable<AppUiEvent> {
        return listOf(
                stopHeading.scheduleButtonClicks,
                stopHeading.locationButtonClicks,
                stopHeading.saveStopButtonClicks,
                stopList.stopSearchedEvent,
                stopList.stopSelectedEvent,
                mapHelper.cameraIdleEvents,
                mapHelper.markerClicks,
                locationEvents,
                slidingPanel.slidingPanelEvents,

                //This delay prevents UI hiccups from occurring when the trip list is refreshed
                nexTripApiHelper.apiResultEvents.delay(500, TimeUnit.MILLISECONDS),

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

    private inner class MtDrawerToggle : ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close) {
        override fun onDrawerClosed(drawerView: View) {
            super.onDrawerClosed(drawerView)
            EZ.hideKeyboard(this@MainActivity)
        }
    }
}
