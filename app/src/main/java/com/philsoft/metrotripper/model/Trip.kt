package com.philsoft.metrotripper.model

data class Trip(val vehicle: Vehicle,
                val actual: Boolean,
                val blockNumber: Int,
                val departureText: String,
                val departureTime: Long,
                val description: String,
                val gate: String,
                val routeDirection: String)