package com.philsoft.metrotripper.database;

import android.database.Cursor;

import com.google.common.collect.Lists;
import com.philsoft.metrotripper.database.contracts.StopContract;
import com.philsoft.metrotripper.model.Stop;

import java.util.List;


public class ModelFactory {


	public static List<Stop> buildStops(Cursor cursor) {
		List<Stop> stops = Lists.newArrayList();
		CursorHelper c = new CursorHelper(cursor);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			long stopId = c.getLong(StopContract.STOP_ID);
			String stopName = c.getString(StopContract.STOP_NAME);
			String stopDesc = c.getString(StopContract.STOP_ID);
			Double stopLat = c.getDouble(StopContract.STOP_LAT);
			Double stopLon = c.getDouble(StopContract.STOP_LON);
			String stopStreet = c.getString(StopContract.STOP_STREET);
			String stopCity = c.getString(StopContract.STOP_CITY);
			String stopRegion = c.getString(StopContract.STOP_REGION);
			String stopPostcode = c.getString(StopContract.STOP_POSTCODE);
			String stopCountry = c.getString(StopContract.STOP_COUNTRY);
			String zoneId = c.getString(StopContract.ZONE_ID);
			int wheelchairBoarding = c.getInteger(StopContract.WHEELCHAIR_BOARDING);
			String stopUrl = c.getString(StopContract.STOP_URL);

			Stop stop = new Stop(stopId, stopName, stopDesc, stopLat, stopLon, stopStreet, stopCity, stopRegion,
					stopPostcode, stopCountry, zoneId, wheelchairBoarding, stopUrl);
			stops.add(stop);
			cursor.moveToNext();
		}
		return stops;
	}
}
