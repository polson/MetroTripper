package com.philsoft.metrotripper.model

import com.google.gson.annotations.SerializedName

data class NexTripResult(
    @SerializedName("departures") val departures: List<Trip> = emptyList()
)
