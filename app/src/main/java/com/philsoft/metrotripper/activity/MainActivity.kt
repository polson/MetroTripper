package com.philsoft.metrotripper.activity

import android.content.res.Configuration
import android.os.Bundle
import android.support.v7.app.ActionBarDrawerToggle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.MapStyleOptions
import com.philsoft.metrotripper.R
import com.philsoft.metrotripper.app.SettingsProvider
import com.philsoft.metrotripper.app.about.AboutDialog
import com.philsoft.metrotripper.app.state.*
import com.philsoft.metrotripper.app.state.transformer.*
import com.philsoft.metrotripper.app.ui.slidingpanel.SlidingPanel
import com.philsoft.metrotripper.app.ui.view.MapVehicleHelper
import com.philsoft.metrotripper.app.ui.view.MapViewHelper
import com.philsoft.metrotripper.app.ui.view.NexTripApiHelper
import com.philsoft.metrotripper.database.DataProvider
import com.philsoft.metrotripper.database.DatabasePopulator
import com.philsoft.metrotripper.prefs.Prefs
import com.philsoft.metrotripper.utils.EZ
import com.philsoft.metrotripper.utils.map.RxLocation
import com.philsoft.metrotripper.utils.ui.Ui
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.merge
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit

class MainActivity : BaseActivity(), OnMapReadyCallback {

    private val drawerToggle by lazy { MtDrawerToggle() }
    private lateinit var mapViewHelper: MapViewHelper
    private lateinit var mapVehicleHelper: MapVehicleHelper

    private val locationEvents by lazy {
        val client = LocationServices.getFusedLocationProviderClient(this)
        RxLocation.locationEvents(client)
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
        setupListeners()
    }

    private fun setupMap(map: GoogleMap) {
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
        map.isMyLocationEnabled = true // show location button
    }

    private fun setupMapViewHelper(map: GoogleMap) {
        val stopBitmap = Ui.createBitmapFromDrawableResource(this, -30, -30, R.drawable.ic_bus_stop)
        val starredBitmap = Ui.createBitmapFromLayoutResource(this, R.layout.starred_stop)
        mapViewHelper = MapViewHelper(stopBitmap, starredBitmap, map)
        mapVehicleHelper = MapVehicleHelper(this, map)
    }

    private fun setupListeners() {
        val dataProvider = DataProvider(this)
        val settingsProvider = SettingsProvider(Prefs.getInstance(this))
        val appStateTransformer = AppStateTransformer(dataProvider, settingsProvider)
        val nexTripApiHelper = NexTripApiHelper()

        val uiEvents: Observable<AppUiEvent> = listOf(
                stopHeading.scheduleButtonClicks
                        .throttleFirst(500, TimeUnit.MILLISECONDS)
                        .map {
                            AppUiEvent.ScheduleButtonClicked
                        }.share(),
                stopHeading.locationButtonClicks.map {
                    AppUiEvent.LocationButtonClicked
                }.share(),
                stopHeading.saveStopButtonClicks.map {
                    AppUiEvent.SaveStopButtonClicked
                }.share(),
                stopList.stopSearchedEvent.map { stopId ->
                    AppUiEvent.StopSearched(stopId)
                }.share(),
                stopList.stopSelectedEvent.map { stop ->
                    AppUiEvent.StopSelectedFromDrawer(stop)
                }.share(),
                mapViewHelper.cameraIdleEvents.map { cameraPosition ->
                    AppUiEvent.CameraIdle(cameraPosition)
                }.share(),
                mapViewHelper.markerClicks.map { marker ->
                    AppUiEvent.MarkerClicked(marker)
                }.share(),
                locationEvents.map { locationResult ->
                    AppUiEvent.InitialLocationUpdate(locationResult)
                }.share(),
                slidingPanel.slidingPanelEvents
                        .filter { panelState -> panelState == SlidingPanel.PanelState.EXPANDED }
                        .map { AppUiEvent.SlidingPanelExpanded }.share(),
                nexTripApiHelper.apiResultObservable.map { event ->
                    when (event) {
                        is NexTripApiHelper.Event.LoadTripsComplete -> AppUiEvent.GetTripsComplete(event.trips)
                        NexTripApiHelper.Event.LoadTripsFailed -> AppUiEvent.GetTripsFailed
                        NexTripApiHelper.Event.LoadTripsInFlight -> AppUiEvent.GetTripsInFlight
                    }
                }.share(),
                Observable.just(AppUiEvent.Initialize).share()
        ).merge()

        val uiEventToAction = ObservableTransformer<AppStateTransformer.AppUiEventWithState, AppAction> { observable ->
            listOf(
                    observable.compose(StopHeadingTransformer()),
                    observable.compose(MapTransformer()),
                    observable.compose(DrawerActionTransformer()),
                    observable.compose(NexTripApiActionTransformer()),
                    observable.compose(SlidingPanelTransformer()),
                    observable.compose(TripListTransformer()),
                    observable.compose(StopListTransformer()),
                    observable.compose(VehicleTransformer())
            ).merge()
        }

        uiEvents.observeOn(AndroidSchedulers.mainThread())
                .compose(appStateTransformer)
                .compose(uiEventToAction)
                .subscribe { appAction ->
                    when (appAction) {
                        is MapAction -> mapViewHelper.render(appAction)
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
