package com.philsoft.metrotripper.repository

import com.philsoft.metrotripper.app.nextrip.NexTripService
import com.philsoft.metrotripper.model.Trip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NexTripRepository(private val nexTripService: NexTripService) {

    suspend fun getTrips(stopId: Long): Result<List<Trip>> = withContext(Dispatchers.IO) {
        try {
            val result = nexTripService.getTrips(stopId)
            Result.success(result.departures)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
