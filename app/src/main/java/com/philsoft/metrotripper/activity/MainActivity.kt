package com.philsoft.metrotripper.activity

import android.content.res.Configuration
import android.location.Location
import android.os.Bundle
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.philsoft.metrotripper.R
import com.philsoft.metrotripper.app.LocationHelper
import com.philsoft.metrotripper.app.SelectedStopProvider
import com.philsoft.metrotripper.app.SettingsProvider
import com.philsoft.metrotripper.app.about.AboutDialog
import com.philsoft.metrotripper.app.drawer.DrawerAdapter
import com.philsoft.metrotripper.app.nextrip.TripAdapter
import com.philsoft.metrotripper.app.ui.MapHelper
import com.philsoft.metrotripper.app.ui.MapVehicleHelper
import com.philsoft.metrotripper.app.ui.StopHelper
import com.philsoft.metrotripper.app.ui.StopInfoHelper
import com.philsoft.metrotripper.database.DataProvider
import com.philsoft.metrotripper.database.DatabasePopulator
import com.philsoft.metrotripper.model.Stop
import com.philsoft.metrotripper.prefs.Prefs
import com.philsoft.metrotripper.utils.EZ
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.drawer.*
import org.apache.commons.lang.StringUtils
import org.jetbrains.anko.toast
import timber.log.Timber
import java.io.IOException

class MainActivity : BaseActivity(), OnMapReadyCallback, LocationHelper.LocationReadyListener, SelectedStopProvider, GoogleMap.OnMarkerClickListener {

    private var drawerToggle: ActionBarDrawerToggle? = null
    private var appHub: AppHub? = null
    private var locationHelper: LocationHelper? = null
    private var mapHelper: MapHelper? = null
    private var mapVehicleHelper: MapVehicleHelper? = null
    private var stopInfoHelper: StopInfoHelper? = null
    private var stopHelper: StopHelper? = null
    private var savedInstanceState: Bundle? = null
    private var drawerAdapter: DrawerAdapter? = null
    private var tripAdapter: TripAdapter? = null
    private var selectedStop: Stop? = null
    private lateinit var dataProvider: DataProvider
    private lateinit var prefs: Prefs
    private lateinit var settingsProvider: SettingsProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.savedInstanceState = savedInstanceState
        setContentView(R.layout.activity_main)
        setupActionBar()

        prefs = Prefs.getInstance(this)
        settingsProvider = SettingsProvider(prefs)
        dataProvider = DataProvider(this)

        leftDrawer.layoutManager = LinearLayoutManager(this)
        setupSlidingPanel()
        populateStops()
        appHub = AppHub(this)
        setupMap()
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

    override fun onStart() {
        super.onStart()
        if (mapHelper != null) {
            // Map was already set up, set the listeners again since onMapReady() is not going to get called
            setupListeners()
        }
    }

    private fun setupActionBar() {
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar!!.setHomeButtonEnabled(true)
        actionBar!!.setDisplayShowTitleEnabled(false)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        saveStopState(outState)
        outState.putString(KEY_PANEL_STATE, panel.panelState.name)
        if (mapHelper != null) { // Check if map is ready
            mapHelper!!.saveState(outState)
            stopInfoHelper!!.saveState(outState)
        }
    }

    private fun saveStopState(outState: Bundle) {
        if (getSelectedStop() != null) {
            outState.putLong(SelectedStopProvider.KEY_STOP_ID, getSelectedStop()!!.stopId)
        }
    }

    private fun restoreState(savedState: Bundle) {
        restoreStopState(savedState)
        mapHelper!!.restoreState(savedState)
        stopInfoHelper!!.restoreState(savedState)
        restorePanelState(savedState)
    }

    private fun restoreStopState(savedInstanceState: Bundle) {
        val selectedStopId = savedInstanceState.getLong(SelectedStopProvider.KEY_STOP_ID)
        val stop = appHub!!.dataProvider.getStopById(selectedStopId)
        if (stop != null) {
            showStop(stop)
        }
    }

    private fun restorePanelState(savedState: Bundle) {
        val panelStateStr = savedState.getString(KEY_PANEL_STATE)
        val panelState = SlidingUpPanelLayout.PanelState.valueOf(panelStateStr)
        panel.panelState = panelState
    }

    private fun restorePanelHeight() {
        panel.panelHeight = resources.getDimensionPixelSize(R.dimen.panel_height)
    }

    private fun setupDrawer() {
        drawerAdapter = DrawerAdapter()
        if (selectedStop != null) {
            if (panel.panelState == SlidingUpPanelLayout.PanelState.HIDDEN) {
                panel.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
                // HIDDEN is broken on SlidingUpPanel 3.0, so use height
                restorePanelHeight()
            }
            selectStopOnMap(selectedStop, false)
        }

        drawerAdapter?.stopSearchedEvent?.subscribe { stopId ->
            Timber.d("Stop selected: $stopId")
            EZ.hideKeyboard(this)
            val stop = appHub!!.dataProvider.getStopById(stopId)
            if (stop != null) {
                selectStopOnMap(stop, true)
            } else {
                toast(R.string.stop_not_found)
            }
        }

        leftDrawer.adapter = drawerAdapter
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
        mapHelper?.centerCameraOnLatLng(stop!!.latLng, animate)
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

    private fun showCurrentLocation() {
        if (locationHelper == null) {
            locationHelper = LocationHelper(this, this)
        }
        locationHelper!!.getLastLocation()
    }

    private fun setupSlidingPanel(): SlidingUpPanelLayout {
        val layoutManager = LinearLayoutManager(this)
        tripList.layoutManager = layoutManager
        tripAdapter = TripAdapter()
        tripList.adapter = tripAdapter

        // Fix issues with touch events when recyclerview is used inside sliding panel
        tripList.setOnTouchListener { v, event ->
            val action = event.action
            when (action) {
                MotionEvent.ACTION_DOWN ->
                    // Disallow ScrollView to intercept touch events.
                    v.parent.requestDisallowInterceptTouchEvent(true)

                MotionEvent.ACTION_UP ->
                    // Allow ScrollView to intercept touch events.
                    v.parent.requestDisallowInterceptTouchEvent(false)
            }
            v.onTouchEvent(event)
            true
        }

        if (selectedStop == null) {
            panel.isTouchEnabled = true
            panel.anchorPoint = 0.5f

            // PanelState.HIDDEN is broken on SlidingUpPanel 3.0, have to use visibility instead
            panel.panelState = SlidingUpPanelLayout.PanelState.HIDDEN
            panel.panelHeight = 0
        }
        return panel
    }

    override fun onLocationReady(location: Location?) {
        if (mapHelper != null) {
            if (location != null) {
                mapHelper!!.centerCameraOnLatLng(LatLng(location.latitude, location.longitude), false)
            } else {
                mapHelper!!.centerCameraOnLatLng(MINNEAPOLIS_LATLNG, false)
            }
        }
    }

    private fun setupMap() {
        val mapFragment = fragmentManager.findFragmentById(R.id.map) as MapFragment
        mapFragment.getMapAsync(this)
    }

    private fun setupListeners() {
        mapHelper!!.addListener(stopHelper)
    }

    override fun onMapReady(map: GoogleMap) {
        Timber.d("OnMapReady")
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
        map.isMyLocationEnabled = true // show location button
        mapHelper = MapHelper(this, map)
        mapVehicleHelper = MapVehicleHelper(this, map)
        stopInfoHelper = StopInfoHelper(this, this, panel, mapHelper!!, settingsProvider, tripAdapter!!)
        stopHelper = StopHelper(this, map, appHub!!.dataProvider, settingsProvider)
        setupDrawer()

        map.setOnMarkerClickListener(this)
        setupListeners()

        if (savedInstanceState != null) {
            restoreState(savedInstanceState!!)
        } else {
            if (selectedStop == null) {
                restoreStopFromPrefs()
                if (selectedStop == null) {
                    showCurrentLocation()
                }
            }
        }
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        if (StringUtils.isNumeric(marker.title)) {
            // Assume this is a stop if the info window is a number
            val stop = appHub!!.dataProvider.getStopById(Integer.valueOf(marker.title)!!.toLong())
            showStop(stop)
            panel.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
        }
        return false
    }

    override fun showStop(stop: Stop?) {
        if (stop != selectedStop) {
            Timber.d("New stop selected: " + stop!!.stopId)
            restorePanelHeight()
            selectedStop = stop
            stopHelper!!.selectStopMarker(stop)
            stopInfoHelper!!.showStopInfo(stop)
        }
    }

    private fun restoreStopFromPrefs() {
        // Restore state from prefs if this is a fresh start of the app
        val stopId = prefs!!.lastStopId
        val stop = appHub!!.dataProvider.getStopById(stopId)
        if (stop != null) {
            selectStopOnMap(stop, false)
        }
    }

    override fun onStop() {
        Timber.d("onStop")
        if (mapHelper != null) {
            mapHelper!!.removeListener(stopHelper)
        }
        if (selectedStop != null) {
            prefs!!.lastStopId = selectedStop!!.stopId
        }
        super.onStop()
    }

    override fun getSelectedStop(): Stop? {
        return selectedStop
    }

    override fun setSelectedStop(stop: Stop) {
        selectedStop = stop
    }

    companion object {

        private val KEY_PANEL_STATE = "KEY_PANEL_STATE"
        private val MINNEAPOLIS_LATLNG = LatLng(44.9799700, -93.2638400)
    }
}
