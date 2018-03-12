package com.philsoft.metrotripper.activity

import android.content.res.Configuration
import android.net.http.SslCertificate.restoreState
import android.os.Bundle
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.LinearLayoutManager
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
import com.philsoft.metrotripper.app.drawer.DrawerAdapter
import com.philsoft.metrotripper.app.state.AppEvent
import com.philsoft.metrotripper.app.state.AppStateManager
import com.philsoft.metrotripper.app.ui.MapVehicleHelper
import com.philsoft.metrotripper.app.ui.view.MapViewHelper
import com.philsoft.metrotripper.app.ui.view.TripListView
import com.philsoft.metrotripper.database.DataProvider
import com.philsoft.metrotripper.database.DatabasePopulator
import com.philsoft.metrotripper.model.Stop
import com.philsoft.metrotripper.prefs.Prefs
import com.philsoft.metrotripper.utils.EZ
import com.philsoft.metrotripper.utils.map.RxLocation
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.TimeUnit

class MainActivity : BaseActivity(), OnMapReadyCallback {

    private var drawerToggle: ActionBarDrawerToggle? = null
    private var mapVehicleHelper: MapVehicleHelper? = null
    private var savedInstanceState: Bundle? = null
    private var drawerAdapter: DrawerAdapter? = null
    private var selectedStop: Stop? = null
    private lateinit var dataProvider: DataProvider
    private lateinit var prefs: Prefs
    private lateinit var settingsProvider: SettingsProvider
    private lateinit var stateManager: AppStateManager
    private lateinit var tripListView: TripListView
    private lateinit var mapViewHelper: MapViewHelper

    private val locationEvents by lazy {
        val client = LocationServices.getFusedLocationProviderClient(this)
        RxLocation.locationEvents(client)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.savedInstanceState = savedInstanceState
        setContentView(R.layout.activity_main)
        setupActionBar()

        prefs = Prefs.getInstance(this)
        settingsProvider = SettingsProvider(prefs)
        dataProvider = DataProvider(this)

        populateStops()
        setupMap()
        setupSlidingPanel()
        setupDrawer()
    }


    private fun setupMap() {
        val mapFragment = fragmentManager.findFragmentById(R.id.map) as MapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        setupViews(map)
        setupListeners()

        Timber.d("OnMapReady")
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
        map.isMyLocationEnabled = true // show location button
        mapVehicleHelper = MapVehicleHelper(this, map)
//        stopHelper = StopHelper(this, this, map, dataProvider, settingsProvider)

        if (savedInstanceState != null) {
            restoreState(savedInstanceState!!)
        } else {
            if (selectedStop == null) {
                restoreStopFromPrefs()
            }
        }
    }

    private fun setupViews(map: GoogleMap) {
        tripListView = TripListView(tripList)
        mapViewHelper = MapViewHelper(this, map)
    }

    private fun setupListeners() {
        stateManager = AppStateManager(this)
        val observables = listOf(
                stopHeading.headingClicks.map { AppEvent.ShowStops },
                stopHeading.scheduleButtonClicks
                        .throttleFirst(500, TimeUnit.MILLISECONDS).map { AppEvent.ShowSchedule },
                stopHeading.locationButtonClicks.map { AppEvent.ShowCurrentStopLocation },
                stopHeading.saveStopButtonClicks.map { AppEvent.SaveStop },
                drawerAdapter?.stopSearchedEvent?.map { stopId -> AppEvent.SearchStop(stopId) },
                mapViewHelper.cameraIdleEvents.map { cameraPosition -> AppEvent.CameraIdle(cameraPosition) },
                locationEvents.map { locationResult -> AppEvent.InitialLocationUpdate(locationResult) },
                mapViewHelper.markerClicks.map { marker -> AppEvent.MarkerClick(marker) }
        )
        Observable.merge(observables).subscribe(stateManager::handleEvent)

        stateManager.mapActions.subscribe(mapViewHelper::render)
        stateManager.stopHeadingActions.subscribe(stopHeading::render)
        stateManager.tripListActions.subscribe(tripListView::render)
    }

    private fun populateStops() {
        val populator = DatabasePopulator(this)
        try {
            if (populator.isTableEmpty()) {
                populator.populateStopsFast()
            } else {
                Timber.d("Unable to populate stops: Stop table not empty")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun setupActionBar() {
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeButtonEnabled(true)
        actionBar.setDisplayShowTitleEnabled(false)
    }

    private fun setupDrawer() {
        stopListRv.layoutManager = LinearLayoutManager(this)
        drawerAdapter = DrawerAdapter()
        if (selectedStop != null) {
            if (panel.panelState == SlidingUpPanelLayout.PanelState.HIDDEN) {
                panel.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
            }
            selectStopOnMap(selectedStop, false)
        }

        drawerAdapter?.stopSearchedEvent?.subscribe { stopId ->
            Timber.d("Stop selected: $stopId")
            EZ.hideKeyboard(this)
            val stop = dataProvider.getStopById(stopId)
            if (stop != null) {
                selectStopOnMap(stop, true)
            } else {
                toast(R.string.stop_not_found)
            }
        }

        stopListRv.adapter = drawerAdapter
        drawerToggle = object : ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close) {

            override fun onDrawerClosed(drawerView: View) {
                super.onDrawerClosed(drawerView)
                EZ.hideKeyboard(this@MainActivity)
            }
        }
        drawerLayout.setDrawerListener(drawerToggle)
        drawerToggle!!.syncState()
    }

    fun selectStopOnMap(stop: Stop?, animate: Boolean) {
        drawerLayout.closeDrawers()
        showStop(stop)
        panel?.panelState = SlidingUpPanelLayout.PanelState.ANCHORED
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawerToggle!!.onConfigurationChanged(newConfig)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_activity, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (drawerToggle!!.onOptionsItemSelected(item)) {
            return true
        }
        when (item.itemId) {
            R.id.about -> {
                AboutDialog().show(fragmentManager, null)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun setupSlidingPanel(): SlidingUpPanelLayout {
        if (selectedStop == null) {
            panel.isTouchEnabled = true
            panel.anchorPoint = 0.5f

            // PanelState.HIDDEN is broken on SlidingUpPanel 3.0, have to use visibility instead
            panel.panelState = SlidingUpPanelLayout.PanelState.HIDDEN
            panel.panelHeight = 0
        }
        return panel
    }

    fun showStop(stop: Stop?) {
        if (stop != selectedStop) {
            Timber.d("New stop selected: " + stop!!.stopId)
            selectedStop = stop
        }
    }

    private fun restoreStopFromPrefs() {
        // Restore state from prefs if this is a fresh start of the app
        val stopId = prefs.lastStopId
        val stop = dataProvider.getStopById(stopId)
        if (stop != null) {
            selectStopOnMap(stop, false)
        }
    }

    override fun onStop() {
        Timber.d("onStop")
        if (selectedStop != null) {
            prefs.lastStopId = selectedStop!!.stopId
        }
        super.onStop()
    }

    fun getSelectedStop(): Stop? {
        return selectedStop
    }

    fun setSelectedStop(stop: Stop) {
        selectedStop = stop
    }
}
