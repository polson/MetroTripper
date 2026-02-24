package com.philsoft.metrotripper.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import java.util.LinkedHashSet

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "defaultBucket")

class Prefs(private val context: Context) {

    companion object {
        private val KEY_SAVED_STOPS = stringPreferencesKey("KEY_SAVED_STOPS")
        private val KEY_STOPS_VERSION = intPreferencesKey("KEY_STOPS_VERSION")
        
        @Volatile
        private var instance: Prefs? = null
        
        fun getInstance(ctx: Context): Prefs {
            return instance ?: synchronized(this) {
                instance ?: Prefs(ctx.applicationContext).also { instance = it }
            }
        }
    }

    suspend fun getStopsVersion(): Int {
        val prefs = context.dataStore.data.first()
        return prefs[KEY_STOPS_VERSION] ?: 0
    }

    suspend fun setStopsVersion(version: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_STOPS_VERSION] = version
        }
    }

    suspend fun getSavedStopIds(): LinkedHashSet<Long> {
        val prefs = context.dataStore.data.first()
        val savedStops = prefs[KEY_SAVED_STOPS] ?: ""
        return savedStops.split(",").filterNot { it.isEmpty() }.mapTo(linkedSetOf()) { it.toLong() }
    }

    suspend fun saveStopId(stopId: Long) {
        val stops = getSavedStopIds()
        stops.add(stopId)
        val newStopsStr = stops.joinToString(",")
        context.dataStore.edit { prefs ->
            prefs[KEY_SAVED_STOPS] = newStopsStr
        }
    }

    suspend fun removeStopId(stopId: Long) {
        val stops = getSavedStopIds()
        val newStopsStr = stops.filterNot { it == stopId }.joinToString(",")
        context.dataStore.edit { prefs ->
            prefs[KEY_SAVED_STOPS] = newStopsStr
        }
    }
}
