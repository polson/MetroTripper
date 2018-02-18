package com.philsoft.metrotripper.activity;

import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.philsoft.metrotripper.R;
import com.philsoft.metrotripper.app.AppHub;
import com.philsoft.metrotripper.app.LocationHelper;
import com.philsoft.metrotripper.app.SelectedStopProvider;
import com.philsoft.metrotripper.app.about.AboutDialog;
import com.philsoft.metrotripper.app.drawer.DrawerAdapter;
import com.philsoft.metrotripper.app.nextrip.TripAdapter;
import com.philsoft.metrotripper.app.ui.MapHelper;
import com.philsoft.metrotripper.app.ui.MapVehicleHelper;
import com.philsoft.metrotripper.app.ui.StopHelper;
import com.philsoft.metrotripper.app.ui.StopInfoHelper;
import com.philsoft.metrotripper.model.Stop;
import com.philsoft.metrotripper.prefs.Prefs;
import com.philsoft.metrotripper.utils.EZ;
import com.philsoft.metrotripper.utils.ui.Ui;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.apache.commons.lang.StringUtils;

import timber.log.Timber;

public class MainActivity extends BaseActivity implements OnMapReadyCallback, LocationHelper.LocationReadyListener, SelectedStopProvider,
		GoogleMap.OnMarkerClickListener {

	private static final String KEY_PANEL_STATE = "KEY_PANEL_STATE";
	private static final LatLng MINNEAPOLIS_LATLNG = new LatLng(44.9799700, -93.2638400);

	private ActionBarDrawerToggle drawerToggle;
	private AppHub appHub;
	private LocationHelper locationHelper;
	private MapHelper mapHelper;
	private MapVehicleHelper mapVehicleHelper;
	private StopInfoHelper stopInfoHelper;
	private StopHelper stopHelper;
	private Bundle savedInstanceState;
	private DrawerAdapter drawerAdapter;
	private TripAdapter tripAdapter;
	private Stop selectedStop;
	private SlidingUpPanelLayout panel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupActionBar();
		this.savedInstanceState = savedInstanceState;
		setContentView(R.layout.activity_main);

		RecyclerView drawerList = Ui.findView(this, R.id.left_drawer);
		drawerList.setLayoutManager(new LinearLayoutManager(this));

		setupSlidingPanel();
		appHub = new AppHub(this);
		setupMap();
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (mapHelper != null) {
			// Map was already set up, set the listeners again since onMapReady() is not going to get called
			setupListeners();
		}
		appHub.getNexTripManager().resumeUpdatingTrips();
	}

	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayShowTitleEnabled(false);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		saveStopState(outState);
		outState.putString(KEY_PANEL_STATE, panel.getPanelState().name());
		if (mapHelper != null) { // Check if map is ready
			mapHelper.saveState(outState);
			stopInfoHelper.saveState(outState);
		}
	}

	private void saveStopState(Bundle outState) {
		if (getSelectedStop() != null) {
			outState.putLong(KEY_STOP_ID, getSelectedStop().stopId);
		}
	}

	private void restoreState(Bundle savedState) {
		restoreStopState(savedState);
		mapHelper.restoreState(savedState);
		stopInfoHelper.restoreState(savedState);
		restorePanelState(savedState);
	}

	private void restoreStopState(Bundle savedInstanceState) {
		long selectedStopId = savedInstanceState.getLong(KEY_STOP_ID);
		Stop stop = appHub.getDataProvider().getStopById(selectedStopId);
		if (stop != null) {
			showStop(stop);
		}
	}

	private void restorePanelState(Bundle savedState) {
		String panelStateStr = savedState.getString(KEY_PANEL_STATE);
		SlidingUpPanelLayout.PanelState panelState = SlidingUpPanelLayout.PanelState.valueOf(panelStateStr);
		panel.setPanelState(panelState);
	}

	private void restorePanelHeight() {
		panel.setPanelHeight(getResources().getDimensionPixelSize(R.dimen.panel_height));
	}

	private void setupDrawer() {
		DrawerLayout drawer = Ui.findView(this, R.id.drawer_layout);
		RecyclerView drawerList = Ui.findView(this, R.id.left_drawer);
		drawerAdapter = new DrawerAdapter(this, drawer, appHub.getDataProvider(), mapHelper, appHub.getSettingsProvider(), panel);
		if (selectedStop != null) {
			if (panel.getPanelState() == SlidingUpPanelLayout.PanelState.HIDDEN) {
				panel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
				// HIDDEN is broken on SlidingUpPanel 3.0, so use height
				restorePanelHeight();
			}
			drawerAdapter.selectStopOnMap(selectedStop, false);
		}
		drawerList.setAdapter(drawerAdapter);
		drawerToggle = new ActionBarDrawerToggle(this, drawer, R.string.drawer_open, R.string.drawer_close) {

			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
			}

			@Override
			public void onDrawerClosed(View drawerView) {
				super.onDrawerClosed(drawerView);
				EZ.hideKeyboard(MainActivity.this);
			}

			@Override
			public void onDrawerStateChanged(int newState) {
				super.onDrawerStateChanged(newState);
				if (newState == DrawerLayout.STATE_SETTLING) {
					drawerAdapter.refresh();
				}
			}
		};
		drawer.setDrawerListener(drawerToggle);
		drawerToggle.syncState();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		drawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_activity, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (drawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		switch (item.getItemId()) {
		case R.id.about:
			new AboutDialog().show(getFragmentManager(), null);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void showCurrentLocation() {
		if (locationHelper == null) {
			locationHelper = new LocationHelper(this, this);
		}
		locationHelper.getLastLocation();
	}

	private SlidingUpPanelLayout setupSlidingPanel() {
		RecyclerView tripList = Ui.findView(this, R.id.stop_info_list);
		LinearLayoutManager layoutManager = new LinearLayoutManager(this);
		tripList.setLayoutManager(layoutManager);
		tripAdapter = new TripAdapter(this);
		tripList.setAdapter(tripAdapter);

		// Fix issues with touch events when recyclerview is used inside sliding panel
		tripList.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
				switch (action) {
				case MotionEvent.ACTION_DOWN:
					// Disallow ScrollView to intercept touch events.
					v.getParent().requestDisallowInterceptTouchEvent(true);
					break;

				case MotionEvent.ACTION_UP:
					// Allow ScrollView to intercept touch events.
					v.getParent().requestDisallowInterceptTouchEvent(false);
					break;
				}
				v.onTouchEvent(event);
				return true;
			}
		});

		panel = Ui.findView(this, R.id.sliding_panel);
		if (selectedStop == null) {
			panel.setTouchEnabled(true);
			panel.setAnchorPoint(0.5f);

			// PanelState.HIDDEN is broken on SlidingUpPanel 3.0, have to use visibility instead
			panel.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
			panel.setPanelHeight(0);
		}
		return panel;
	}

	@Override
	public void onLocationReady(Location location) {
		if (mapHelper != null) {
			if (location != null) {
				mapHelper.centerCameraOnLatLng(new LatLng(location.getLatitude(), location.getLongitude()), false);
			} else {
				mapHelper.centerCameraOnLatLng(MINNEAPOLIS_LATLNG, false);
			}
		}
	}

	private void setupMap() {
		MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);
	}

	private void setupListeners() {
		mapHelper.addListener(stopHelper);
		appHub.getNexTripManager().addListener(stopInfoHelper);
		appHub.getNexTripManager().addListener(mapVehicleHelper);
	}

	@Override
	public void onMapReady(GoogleMap map) {
		Timber.d("OnMapReady");
		map.setMyLocationEnabled(true); // show location button
		mapHelper = new MapHelper(this, map);
		mapVehicleHelper = new MapVehicleHelper(this, map);
		stopInfoHelper = new StopInfoHelper(this, panel, appHub.getNexTripManager(), mapHelper, appHub.getSettingsProvider(), tripAdapter);
		stopHelper = new StopHelper(this, map, appHub.getDataProvider(), appHub.getSettingsProvider());
		setupDrawer();

		map.setOnMarkerClickListener(this);
		setupListeners();

		if (savedInstanceState != null) {
			restoreState(savedInstanceState);
		} else {
			if (selectedStop == null) {
				restoreStopFromPrefs();
				if (selectedStop == null) {
					showCurrentLocation();
				}
			}
		}
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		if (StringUtils.isNumeric(marker.getTitle())) {
			// Assume this is a stop if the info window is a number
			Stop stop = appHub.getDataProvider().getStopById(Integer.valueOf(marker.getTitle()));
			showStop(stop);
			panel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
		}
		return false;
	}

	public void showStop(Stop stop) {
		if (!stop.equals(selectedStop)) {
			Timber.d("New stop selected: " + stop.stopId);
			restorePanelHeight();
			selectedStop = stop;
			stopHelper.selectStopMarker(stop);
			stopInfoHelper.showStopInfo(stop);
			appHub.getNexTripManager().stopUpdatingTrips();
		}
	}

	private void restoreStopFromPrefs() {
		// Restore state from prefs if this is a fresh start of the app
		long stopId = Prefs.getLastStopId(this);
		Stop stop = appHub.getDataProvider().getStopById(stopId);
		if (stop != null) {
			drawerAdapter.selectStopOnMap(stop, false);
		}
	}

	@Override
	protected void onStop() {
		Timber.d("onStop");
		if (mapHelper != null && appHub.getNexTripManager() != null) {
			mapHelper.removeListener(stopHelper);
			appHub.getNexTripManager().removeListener(stopInfoHelper);
			appHub.getNexTripManager().removeListener(mapVehicleHelper);
			appHub.getNexTripManager().pauseUpdatingTrips();
		}
		if (selectedStop != null) {
			Prefs.saveLastStopId(this, selectedStop.stopId);
		}
		super.onStop();
	}

	@Override
	public Stop getSelectedStop() {
		return selectedStop;
	}

	@Override
	public void setSelectedStop(Stop stop) {
		selectedStop = stop;
	}
}
