package com.philsoft.metrotripper.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NexTripResult(
    @SerialName("departures") val departures: List<Trip> = emptyList()
)
