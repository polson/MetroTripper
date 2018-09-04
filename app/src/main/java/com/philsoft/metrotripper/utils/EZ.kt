package com.philsoft.metrotripper.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager.NameNotFoundException
import android.view.inputmethod.InputMethodManager
import com.google.android.gms.maps.model.LatLng
import com.philsoft.metrotripper.app.nextrip.constants.Regex
import timber.log.Timber
import java.util.regex.Pattern

object EZ {
    private const val METERS_PER_DEGREE = 110500 //Approximate number of meters in 1 degree of longitude

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
        return try {
            val info = pm.getPackageInfo(packageName, 0)
            info.versionName
        } catch (e: NameNotFoundException) {
            "N/A"
        }
    }
}
