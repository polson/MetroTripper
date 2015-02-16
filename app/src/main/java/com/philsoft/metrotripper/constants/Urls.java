package com.philsoft.metrotripper.constants;

import android.net.Uri;

/**
 * Created by polson on 10/14/14.
 */
public class Urls {

	private static final String BASE_URL = "svc.metrotransit.org";


	public static String getVehicleLocationsUrl(String route) {
		Uri.Builder builder = new Uri.Builder().scheme("http")
				.authority(BASE_URL)
				.appendPath("NexTrip")
				.appendPath("VehicleLocations")
				.appendPath(route)
				.appendQueryParameter("format", "json");
		return builder.build().toString();
	}

	public static String getNexTripUrl(long stopId) {
		Uri.Builder builder = new Uri.Builder().scheme("http")
				.authority(BASE_URL)
				.appendPath("NexTrip")
				.appendPath(String.valueOf(stopId))
				.appendQueryParameter("format", "json");
		return builder.build().toString();
	}
}
