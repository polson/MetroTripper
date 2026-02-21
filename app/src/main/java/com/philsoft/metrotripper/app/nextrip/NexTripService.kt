package com.philsoft.metrotripper.app.nextrip

import com.philsoft.metrotripper.model.NexTripResult
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface NexTripService {

    companion object {
        private const val NEXTRIP_URL = "https://svc.metrotransit.org/"

        fun create(): NexTripService {
            val retrofit = Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create(NexTripGsonFactory.getGson()))
                    .baseUrl(NEXTRIP_URL)
                    .build()
            return retrofit.create(NexTripService::class.java)
        }
    }

    @GET("nextrip/{stopId}")
    suspend fun getTrips(@Path("stopId") stopId: Long): NexTripResult
}
