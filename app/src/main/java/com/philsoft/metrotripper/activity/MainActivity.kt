package com.philsoft.metrotripper.activity

import android.content.res.Configuration
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
import com.philsoft.metrotripper.app.about.AboutDialog
import com.philsoft.metrotripper.app.drawer.DrawerAdapter
import com.philsoft.metrotripper.app.state.*
import com.philsoft.metrotripper.app.ui.view.MapViewHelper
import com.philsoft.metrotripper.app.ui.view.TripListView
import com.philsoft.metrotripper.database.DatabasePopulator
import com.philsoft.metrotripper.utils.EZ
import com.philsoft.metrotripper.utils.map.RxLocation
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import io.reactivex.rxkotlin.merge
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import java.util.concurrent.TimeUnit

class MainActivity : BaseActivity(), OnMapReadyCallback {

    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var drawerAdapter: DrawerAdapter
    private lateinit var stateManager: AppStateManager
    private lateinit var tripListView: TripListView
    private lateinit var mapViewHelper: MapViewHelper

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

    private fun populateStops() {
        DatabasePopulator(this).apply {
            if (isTableEmpty()) {
                populateStopsFast()
            } else {
                Timber.d("Unable to populate stops: Stop table not empty")
            }
        }
    }

    private fun setupDrawer() {
        stopListRv.layoutManager = LinearLayoutManager(this)
        drawerAdapter = DrawerAdapter()

        stopListRv.adapter = drawerAdapter
        drawerToggle = object : ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close) {

            override fun onDrawerClosed(drawerView: View) {
                super.onDrawerClosed(drawerView)
                EZ.hideKeyboard(this@MainActivity)
            }
        }
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
    }

    private fun setupMapFragment() {
        val mapFragment = fragmentManager.findFragmentById(R.id.map) as MapFragment
        mapFragment.getMapAsync(this)
    }

    private fun setupSlidingPanel(): SlidingUpPanelLayout {
        panel.isTouchEnabled = true
        panel.anchorPoint = 0.5f
        panel.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
        return panel
    }

    override fun onMapReady(map: GoogleMap) {
        setupMap(map)
        setupViews(map)
        setupListeners()
    }

    private fun setupMap(map: GoogleMap) {
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
        map.isMyLocationEnabled = true // show location button
    }

    private fun setupViews(map: GoogleMap) {
        tripListView = TripListView(tripList)
        mapViewHelper = MapViewHelper(this, map)
    }

    private fun setupListeners() {
        val uiEvents = listOf(
                stopHeading.headingClicks.map {
                    AppUiEvent.HeadingButtonClicked
                },
                stopHeading.scheduleButtonClicks
                        .throttleFirst(500, TimeUnit.MILLISECONDS)
                        .map {
                            AppUiEvent.ScheduleButtonClicked
                        },
                stopHeading.locationButtonClicks.map {
                    AppUiEvent.LocationButtonClicked
                },
                stopHeading.saveStopButtonClicks.map {
                    AppUiEvent.SaveStopButtonClicked
                },
                drawerAdapter.searchStopEvent.map { stopId ->
                    AppUiEvent.StopSearched(stopId)
                },
                mapViewHelper.cameraIdleEvents.map { cameraPosition ->
                    AppUiEvent.CameraIdle(cameraPosition)
                },
                mapViewHelper.markerClicks.map { marker ->
                    AppUiEvent.MarkerClicked(marker)
                },
                locationEvents.map { locationResult ->
                    AppUiEvent.InitialLocationUpdate(locationResult)
                }
        )

        stateManager = AppStateManager(this, uiEvents.merge())
        uiEvents.merge()
                .publish { shared ->
                    listOf(
                            shared.ofType(AppUiEvent.LocationButtonClicked::class.java).compose(stateManager.showCurrentStopTransformer),
                            shared.ofType(AppUiEvent.StopSearched::class.java).compose(stateManager.searchStopTransformer),
                            shared.ofType(AppUiEvent.MarkerClicked::class.java).compose(stateManager.markerClickTransformer),
                            shared.ofType(AppUiEvent.ScheduleButtonClicked::class.java).compose(stateManager.showScheduleTransformer),
                            shared.ofType(AppUiEvent.CameraIdle::class.java).compose(stateManager.cameraIdleTransformer),
                            shared.ofType(AppUiEvent.SaveStopButtonClicked::class.java).compose(stateManager.toggleCurrenStopSavedTransformer),
                            shared.ofType(AppUiEvent.InitialLocationUpdate::class.java).compose(stateManager.locationUpdateTransformer)
                    ).merge()
                }
                .subscribe { appAction ->
                    when (appAction) {
                        is MapAction -> mapViewHelper.render(appAction)
                        is StopHeadingAction -> stopHeading.render(appAction)
                        is TripListAction -> tripListView.render(appAction)
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
                AboutDialog().show(fragmentManager, null)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
