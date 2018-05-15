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
import com.philsoft.metrotripper.app.SettingsProvider
import com.philsoft.metrotripper.app.about.AboutDialog
import com.philsoft.metrotripper.app.drawer.DrawerAdapter
import com.philsoft.metrotripper.app.state.*
import com.philsoft.metrotripper.app.state.transformer.DrawerActionTransformer
import com.philsoft.metrotripper.app.state.transformer.MapTransformer
import com.philsoft.metrotripper.app.state.transformer.StopHeadingTransformer
import com.philsoft.metrotripper.app.ui.view.MapViewHelper
import com.philsoft.metrotripper.app.ui.view.TripListView
import com.philsoft.metrotripper.database.DataProvider
import com.philsoft.metrotripper.database.DatabasePopulator
import com.philsoft.metrotripper.prefs.Prefs
import com.philsoft.metrotripper.utils.EZ
import com.philsoft.metrotripper.utils.map.RxLocation
import com.philsoft.metrotripper.utils.ui.Ui
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.merge
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import java.util.concurrent.TimeUnit

class MainActivity : BaseActivity(), OnMapReadyCallback {

    private val drawerAdapter = DrawerAdapter()
    private val drawerToggle by lazy { MtDrawerToggle() }
    private val tripListView by lazy { TripListView(tripList) }
    private lateinit var mapViewHelper: MapViewHelper


    //Services
    private val dataProvider by lazy { DataProvider(this) }
    private val settingsProvider by lazy { SettingsProvider(Prefs.getInstance(this)) }
    private val stateTransformer by lazy { AppStateTransformer(dataProvider) }

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
        stopListRv.adapter = drawerAdapter
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
    }

    private inner class MtDrawerToggle : ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close) {
        override fun onDrawerClosed(drawerView: View) {
            super.onDrawerClosed(drawerView)
            EZ.hideKeyboard(this@MainActivity)
        }
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
        val stopBitmap = Ui.createBitmapFromDrawableResource(this, -30, -30, R.drawable.ic_bus_stop)
        val starredBitmap = Ui.createBitmapFromLayoutResource(this, R.layout.starred_stop)
        mapViewHelper = MapViewHelper(stopBitmap, starredBitmap, map)
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
        ).merge().share()

        val uiEventToAction = ObservableTransformer<AppStateTransformer.AppUiEventWithState, AppAction> { observable ->
            listOf(
                    observable.compose(StopHeadingTransformer(settingsProvider)),
                    observable.compose(MapTransformer(settingsProvider, dataProvider)),
                    observable.compose(DrawerActionTransformer())
            ).merge()
        }

        uiEvents
                .compose(stateTransformer)
                .compose(uiEventToAction)
                .subscribe { appAction ->
                    Timber.d("Action: " + appAction)
                    render(appAction)
                }
    }

    private fun render(appAction: AppAction?) {
        when (appAction) {
            is MapAction -> mapViewHelper.render(appAction)
            is StopHeadingAction -> stopHeading.render(appAction)
            is TripListAction -> tripListView.render(appAction)
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
