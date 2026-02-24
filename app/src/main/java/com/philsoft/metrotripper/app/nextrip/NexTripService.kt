package com.philsoft.metrotripper.app.nextrip

import com.philsoft.metrotripper.model.NexTripResult
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface NexTripService {

    companion object {
        private const val NEXTRIP_URL = "https://svc.metrotransit.org/"

        fun create(): NexTripService {
            val json = Json { 
                ignoreUnknownKeys = true
                coerceInputValues = true
            }
            val contentType = "application/json".toMediaType()
            val retrofit = Retrofit.Builder()
                    .addConverterFactory(json.asConverterFactory(contentType))
                    .baseUrl(NEXTRIP_URL)
                    .build()
            return retrofit.create(NexTripService::class.java)
        }
    }

    @GET("nextrip/{stopId}")
    suspend fun getTrips(@Path("stopId") stopId: Long): NexTripResult
}
