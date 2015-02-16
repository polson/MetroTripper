package com.philsoft.metrotripper.model;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by polson on 1/23/15.
 */
public class Vehicle {
	public final String route;
	public final String terminal;
	public final double heading;
	public final double latitude;
	public final double longitude;

	public Vehicle(String route, String terminal, double heading, double latitude, double longitude) {
		this.route = route;
		this.terminal = terminal;
		this.heading = heading;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	@Override
	public String toString() {
		return "VehicleLocation{" +
				"route='" + route + '\'' +
				", terminal='" + terminal + '\'' +
				", heading=" + heading +
				", latitude=" + latitude +
				", longitude=" + longitude +
				'}';
	}

	public String getRouteAndTerminal() {
		return route + terminal;
	}

	public LatLng getLatLng() {
		return new LatLng(latitude, longitude);
	}
}
