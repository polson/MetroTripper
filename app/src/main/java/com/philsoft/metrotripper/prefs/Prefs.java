package com.philsoft.metrotripper.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.common.collect.Sets;

import org.apache.commons.lang.StringUtils;

import java.util.LinkedHashSet;
import java.util.Set;


public class Prefs {


	private static final String DEFAULT_PREFS_BUCKET = "defaultBucket";
	private static final String KEY_SAVED_STOPS = "KEY_SAVED_STOPS";
	private static final String KEY_LAST_STOP_ID = "KEY_LAST_STOP_ID";

	private static SharedPreferences getPrefs(Context ctx) {
		return ctx.getSharedPreferences(DEFAULT_PREFS_BUCKET, Context.MODE_PRIVATE);
	}

	public static void saveStopId(Context context, long stopId) {
		Set<Long> savedStops = getSavedStopIds(context);
		savedStops.add(stopId);
		String joinedStops = StringUtils.join(savedStops, ",");
		getPrefs(context).edit().putString(KEY_SAVED_STOPS, joinedStops).commit();
	}

	public static LinkedHashSet<Long> getSavedStopIds(Context context) {
		String joinedStops = getPrefs(context).getString(KEY_SAVED_STOPS, "");
		LinkedHashSet<Long> stopIds = Sets.newLinkedHashSet();
		for (String stopIdStr : StringUtils.split(joinedStops, ",")) {
			stopIds.add(Long.valueOf(stopIdStr));
		}
		return stopIds;
	}

	public static void removeStopId(Context context, long stopId) {
		Set<Long> savedStops = getSavedStopIds(context);
		savedStops.remove(stopId);
		String joinedStops = StringUtils.join(savedStops, ",");
		getPrefs(context).edit().putString(KEY_SAVED_STOPS, joinedStops).commit();
	}

	public static void saveLastStopId(Context context, long stopId) {
		getPrefs(context).edit().putLong(KEY_LAST_STOP_ID, stopId).commit();
	}

	public static long getLastStopId(Context context) {
		return getPrefs(context).getLong(KEY_LAST_STOP_ID, -1);
	}
}
