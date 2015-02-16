package com.philsoft.metrotripper.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.format.DateUtils;
import android.view.inputmethod.InputMethodManager;

import com.google.android.gms.maps.model.LatLng;
import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;
import com.philsoft.metrotripper.constants.Regex;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by polson on 10/14/14.
 */
public class EZ {
	private final static Logger log = LoggerManager.getLogger(EZ.class);
	private final static int METERS_PER_DEGREE = 110500; //Approximate number of meters in 1 degree of longitude

	public static String formatRelativeTime(Date timestamp) {
		return DateUtils.getRelativeTimeSpanString(timestamp.getTime(), new Date().getTime(), 0).toString();
	}

	public static long parseLocationTime(String locationTime) {
		Pattern p = Pattern.compile(Regex.LOCATION_TIME);
		Matcher m = p.matcher(locationTime);
		if (m.matches()) {
			return Long.valueOf(m.group(1));
		}
		log.w("Unable to parse location time");
		return 0;
	}

	public static double getDistanceInDegrees(LatLng currentPos, LatLng newPos) {
		return Math.abs(Math.sqrt((currentPos.latitude - newPos.latitude) * (currentPos.latitude - newPos.latitude) + (currentPos.longitude - newPos.longitude) * (currentPos.longitude - newPos.longitude)));
	}

	public static double getDistanceInMeters(LatLng currentPos, LatLng newPos) {
		double degrees = getDistanceInDegrees(currentPos, newPos);
		return METERS_PER_DEGREE * degrees;
	}

	public static void hideKeyboard(Activity activity) {
		InputMethodManager im = (InputMethodManager) activity.getApplicationContext().getSystemService(
				Context.INPUT_METHOD_SERVICE);
		im.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(),
				InputMethodManager.HIDE_NOT_ALWAYS);
	}

	public static String getAppVersion(Context context) {
		PackageManager pm = context.getPackageManager();
		String packageName = context.getPackageName();
		String versionName;
		try {
			PackageInfo info = pm.getPackageInfo(packageName, 0);
			versionName = info.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			versionName = "N/A";
		}
		return versionName;
	}
}
