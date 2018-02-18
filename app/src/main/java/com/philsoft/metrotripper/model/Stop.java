package com.philsoft.metrotripper.model;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by polson on 1/19/15.
 */
public class Stop {

	public final long stopId;
	public final String stopName;
	public final String stopDesc;
	public final Double stopLat;
	public final Double stopLon;
	public final int wheelchairBoarding;
	public final String stopUrl;

	public Stop(long stopId, String stopName, String stopDesc, Double stopLat, Double stopLon, int wheelchairBoarding, String stopUrl) {
		this.stopId = stopId;
		this.stopName = stopName;
		this.stopDesc = stopDesc;
		this.stopLat = stopLat;
		this.stopLon = stopLon;
		this.wheelchairBoarding = wheelchairBoarding;
		this.stopUrl = stopUrl;
	}

	public LatLng getLatLng() {
		return new LatLng(stopLat, stopLon);
	}

	@Override
	public String toString() {
		return "Stop{" +
				"stopId=" + stopId +
				", stopName='" + stopName + '\'' +
				", stopDesc='" + stopDesc + '\'' +
				", stopLat=" + stopLat +
				", stopLon=" + stopLon +
				", wheelchairBoarding=" + wheelchairBoarding +
				", stopUrl='" + stopUrl + '\'' +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Stop stop = (Stop) o;

		if (stopId != stop.stopId)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return (int) (stopId ^ (stopId >>> 32));
	}
}
