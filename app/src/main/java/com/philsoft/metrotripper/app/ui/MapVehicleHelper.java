package com.philsoft.metrotripper.app.ui;

import android.app.Activity;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.philsoft.metrotripper.R;
import com.philsoft.metrotripper.app.nextrip.NexTripManager;
import com.philsoft.metrotripper.model.Trip;
import com.philsoft.metrotripper.model.Vehicle;
import com.philsoft.metrotripper.utils.map.MapUtils;
import com.philsoft.metrotripper.utils.ui.Ui;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import timber.log.Timber;

/**
 * Created by polson on 1/28/15.
 */
public class MapVehicleHelper implements NexTripManager.NexTripListener {

	private static final int FADE_DURATION = 1000;
	public static final int LOCATION_UPDATE_INTERVAL_MS = 31 * 1000; // ms

	private Activity activity;
	private GoogleMap map;
	private Set<VehicleMarker> vehicleMarkers = Sets.newHashSet();

	public MapVehicleHelper(Activity activity, GoogleMap map) {
		this.activity = activity;
		this.map = map;
	}

	public void displayVehicleMarkers(List<Trip> trips) {
		List<Vehicle> vehicles = getVehiclesForTrips(trips);

		// Fade out existing vehicles
		for (VehicleMarker vehicleMarker : vehicleMarkers) {
			MapUtils.fadeOutMarkerAndRemove(vehicleMarker.marker, FADE_DURATION);
		}
		vehicleMarkers.clear();

		// Fade in new vehicles
		for (Vehicle vehicle : vehicles) {
			VehicleMarker vehicleMarker = new VehicleMarker(vehicle, createVehicleMarker(vehicle));
			vehicleMarkers.add(vehicleMarker);
			MapUtils.fadeInMarker(vehicleMarker.marker, FADE_DURATION);
		}
	}

	private List<Vehicle> getVehiclesForTrips(List<Trip> trips) {
		List<Vehicle> vehicles = Lists.newArrayList();
		for (Trip trip : trips) {
			Vehicle vehicle = trip.vehicle;
			if (vehicle.latitude != 0 && vehicle.longitude != 0) {
				vehicles.add(vehicle);
			}
		}
		return vehicles;
	}

	public Marker createVehicleMarker(Vehicle vehicle) {
		View vehicleView = buildVehicleView(vehicle);
		Marker marker = map.addMarker(new MarkerOptions().anchor(0.0f, 1.0f) // Anchors the marker on the bottom left
				.title(activity.getString(R.string.vehicle) + " " + vehicle.route + vehicle.terminal).position(
						new LatLng(vehicle.latitude, vehicle.longitude)).icon(
						BitmapDescriptorFactory.fromBitmap(Ui.createBitmapFromView(activity, vehicleView))));
		return marker;
	}

	private View buildVehicleView(Vehicle vehicle) {
		View view = activity.getLayoutInflater().inflate(R.layout.vehicle, null);
		Ui.setText(view, R.id.vehicle_number, vehicle.route + vehicle.terminal);
		return view;
	}

	@Override
	public void onNexTripLoadComplete(List<Trip> trips) {
		displayVehicleMarkers(trips);
	}

	@Override
	public void onNexTripLoadFailed(String message) {
		Timber.w("Unable to load nexTrip: " + message);
		Toast.makeText(activity, R.string.unable_to_load_trips, Toast.LENGTH_SHORT);
	}

	@Override
	public void onNexTripLoadingStopped() {
		for (VehicleMarker vehicleMarker : vehicleMarkers) {
			vehicleMarker.marker.remove();
		}
		vehicleMarkers.clear();
	}

	private class VehicleMarker {
		public Vehicle vehicle;
		public Marker marker;

		private VehicleMarker(Vehicle vehicle, Marker marker) {
			this.vehicle = vehicle;
			this.marker = marker;
		}
	}
}
