package com.philsoft.metrotripper.app.nextrip

import com.philsoft.metrotripper.model.Trip
import io.reactivex.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface NexTripService {

    companion object {
        private const val NEXTRIP_URL = "http://svc.metrotransit.org/NexTrip/"

        fun create(): NexTripService {
            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
                    .addConverterFactory(GsonConverterFactory.create(NexTripGsonFactory.getGson()))
                    .baseUrl(NEXTRIP_URL)
                    .build()
            return retrofit.create(NexTripService::class.java)
        }
    }

    @GET("{stopId}?format=json")
    fun getTrips(@Path("stopId") stopId: Long): Observable<List<Trip>>
}
