package com.philsoft.metrotripper.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.philsoft.metrotripper.app.nextrip.NexTripService
import com.philsoft.metrotripper.database.AppDatabase
import com.philsoft.metrotripper.prefs.Prefs
import com.philsoft.metrotripper.repository.NexTripRepository
import com.philsoft.metrotripper.repository.StopsRepository

class MainViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            val stopDao = AppDatabase.getInstance(context).stopDao()
            val prefs = Prefs.getInstance(context)
            val stopsRepository = StopsRepository(stopDao, prefs)
            val nexTripService = NexTripService.create()
            val nexTripRepository = NexTripRepository(nexTripService)
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(stopsRepository, nexTripRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
