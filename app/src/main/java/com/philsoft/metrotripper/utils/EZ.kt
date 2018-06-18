package com.philsoft.metrotripper.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.text.format.DateUtils
import android.view.inputmethod.InputMethodManager

import com.google.android.gms.maps.model.LatLng
import com.philsoft.metrotripper.app.nextrip.constants.Regex

import java.util.Date
import java.util.regex.Matcher
import java.util.regex.Pattern

import timber.log.Timber

object EZ {
    private val METERS_PER_DEGREE = 110500 //Approximate number of meters in 1 degree of longitude

    fun formatRelativeTime(timestamp: Date): String {
        return DateUtils.getRelativeTimeSpanString(timestamp.time, Date().time, 0).toString()
    }

    fun parseLocationTime(locationTime: String): Long {
        val p = Pattern.compile(Regex.LOCATION_TIME)
        val m = p.matcher(locationTime)
        if (m.matches()) {
            return java.lang.Long.valueOf(m.group(1))!!
        }
        Timber.w("Unable to parse location time")
        return 0
    }

    fun getDistanceInDegrees(currentPos: LatLng, newPos: LatLng): Double {
        return Math.abs(Math.sqrt((currentPos.latitude - newPos.latitude) * (currentPos.latitude - newPos.latitude) + (currentPos.longitude - newPos.longitude) * (currentPos.longitude - newPos.longitude)))
    }

    fun getDistanceInMeters(currentPos: LatLng, newPos: LatLng): Double {
        val degrees = getDistanceInDegrees(currentPos, newPos)
        return METERS_PER_DEGREE * degrees
    }

    fun hideKeyboard(activity: Activity) {
        val im = activity.applicationContext.getSystemService(
                Context.INPUT_METHOD_SERVICE) as InputMethodManager
        im.hideSoftInputFromWindow(activity.window.decorView.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS)
    }

    fun getAppVersion(context: Context): String {
        val pm = context.packageManager
        val packageName = context.packageName
        var versionName: String
        try {
            val info = pm.getPackageInfo(packageName, 0)
            versionName = info.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            versionName = "N/A"
        }

        return versionName
    }
}
