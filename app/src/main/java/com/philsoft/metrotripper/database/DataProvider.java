package com.philsoft.metrotripper.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;
import com.philsoft.metrotripper.database.contracts.StopContract;
import com.philsoft.metrotripper.model.Stop;

import java.util.List;


public class DataProvider {

	private final Logger log = LoggerManager.getLogger(getClass());

	private DatabaseHelper dbHelper;
	private SQLiteDatabase db;


	public DataProvider(Context context) {
		dbHelper = DatabaseHelper.getInstance(context);
		db = dbHelper.getWritableDatabase();
	}

	public Stop getStopById(long stopId) {
		Stop stop = null;
		String where = StopContract.STOP_ID + "=?";
		String[] whereArgs = new String[] { String.valueOf(stopId) };
		Cursor cursor = db.query(StopContract.TABLE_NAME, StopContract.ALL_COLUMNS, where, whereArgs, null, null, null);
		try {
			List<Stop> stops = ModelFactory.buildStops(cursor);
			if (!stops.isEmpty()) {
				stop = stops.get(0);
			}
		} finally {
			cursor.close();
		}
		return stop;
	}

	public List<Stop> getClosestStops(double lat, double lon, int max) {
		List<Stop> stops;

		String sql = "SELECT  * "
				+ " FROM " + StopContract.TABLE_NAME
				+ " ORDER BY ABS(" + StopContract.STOP_LAT + " - " + lat + ") + ABS(" + StopContract.STOP_LON + " - " + lon + ") ASC"
				+ " LIMIT " + max;

		Cursor cursor = db.rawQuery(sql, null);
		try {
			stops = ModelFactory.buildStops(cursor);
		} finally {
			cursor.close();
		}
		return stops;
	}
}
