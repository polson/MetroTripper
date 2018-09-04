package com.philsoft.metrotripper.prefs

import android.content.Context
import org.apache.commons.lang.StringUtils
import java.util.*


class Prefs private constructor(context: Context) {
    private val prefs = context.getSharedPreferences(DEFAULT_PREFS_BUCKET, Context.MODE_PRIVATE)

    companion object {
        private const val DEFAULT_PREFS_BUCKET = "defaultBucket"
        private const val KEY_SAVED_STOPS = "KEY_SAVED_STOPS"

        private var instance: Prefs? = null
        @Synchronized
        @JvmStatic
        fun getInstance(ctx: Context): Prefs {
            if (instance == null) {
                instance = Prefs(ctx.applicationContext)
            }
            return instance!!
        }
    }

    private var savedStops: String
        get() = prefs.getString(KEY_SAVED_STOPS, "")
        set(stops) = prefs.edit().putString(KEY_SAVED_STOPS, stops).apply()

    fun saveStopId(stopId: Long) {
        val stops = getSavedStopIds()
        stops.add(stopId)
        savedStops = StringUtils.join(stops, ",")
    }

    fun getSavedStopIds(): LinkedHashSet<Long> {
        return savedStops.split(",").filterNot { it.isEmpty() }.mapTo(linkedSetOf()) { it.toLong() }
    }

    fun removeStopId(stopId: Long) {
        val stops = getSavedStopIds().filterNot { it == stopId }.joinToString(",")
        savedStops = stops
    }
}
