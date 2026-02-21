package com.philsoft.metrotripper.model

import com.google.gson.annotations.SerializedName

data class Trip(
    @SerializedName("actual") val actual: Boolean,
    @SerializedName("departure_text") val departureText: String,
    @SerializedName("departure_time") val departureTime: Long,
    @SerializedName("description") val description: String,
    @SerializedName("gate") val gate: String,
    @SerializedName("route_id") val route: String,
    @SerializedName("direction_text") val routeDirection: String,
    @SerializedName("terminal") val terminal: String,
    // These are no longer present in V2 Departure object, keeping for compatibility
    val blockNumber: Int = 0,
    val vehicleHeading: Float = 0f,
    val vehicleLatitude: Float = 0f,
    val vehicleLongitude: Float = 0f
)
