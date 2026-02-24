package com.philsoft.metrotripper.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Trip(
    val actual: Boolean = false,
    @SerialName("departure_text") val departureText: String = "",
    @SerialName("departure_time") private val _departureTimeRaw: Long = 0L,
    val description: String = "",
    val gate: String = "",
    @SerialName("route_id") val route: String = "",
    @SerialName("direction_text") private val _directionTextRaw: String = "",
    val terminal: String = "",
    // These are no longer present in V2 Departure object, keeping for compatibility
    val blockNumber: Int = 0,
    val vehicleHeading: Float = 0f,
    val vehicleLatitude: Float = 0f,
    val vehicleLongitude: Float = 0f
) {
    val departureTime: Long
        get() = if (_departureTimeRaw > 0) _departureTimeRaw * 1000L else 0L

    val routeDirection: String
        get() = when (_directionTextRaw) {
            "NB" -> "NORTHBOUND"
            "SB" -> "SOUTHBOUND"
            "EB" -> "EASTBOUND"
            "WB" -> "WESTBOUND"
            else -> _directionTextRaw.ifEmpty { "UNKNOWN" }
        }
}
