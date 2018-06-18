package com.philsoft.metrotripper.model

data class Trip(val actual: Boolean,
                val blockNumber: Int,
                val departureText: String,
                val departureTime: Long,
                val description: String,
                val gate: String,
                val route: String,
                val routeDirection: String,
                val terminal: String,
                val vehicleHeading: Float,
                val vehicleLatitude: Float,
                val vehicleLongitude: Float)

