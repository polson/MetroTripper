package com.philsoft.metrotripper.app.nextrip

import com.google.gson.*
import com.philsoft.metrotripper.model.Trip
import java.lang.reflect.Type

object NexTripGsonFactory {
    fun getGson(): Gson {
        return GsonBuilder()
                .registerTypeAdapter(Trip::class.java, deserializer)
                .create()
    }

    private var deserializer = object : JsonDeserializer<Trip> {
        val gson = Gson()

        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Trip {
            val jsonObject = json.asJsonObject

            // Convert departure_time from seconds to milliseconds
            if (jsonObject.has("departure_time") && !jsonObject.get("departure_time").isJsonNull) {
                val seconds = jsonObject.get("departure_time").asLong
                jsonObject.add("departure_time", JsonPrimitive(seconds * 1000))
            } else {
                jsonObject.add("departure_time", JsonPrimitive(0L))
            }

            // Map direction_text to old API format
            if (jsonObject.has("direction_text") && !jsonObject.get("direction_text").isJsonNull) {
                val directionText = jsonObject.get("direction_text").asString
                val mappedDirection = when (directionText) {
                    "NB" -> "NORTHBOUND"
                    "SB" -> "SOUTHBOUND"
                    "EB" -> "EASTBOUND"
                    "WB" -> "WESTBOUND"
                    else -> "UNKNOWN"
                }
                jsonObject.add("direction_text", JsonPrimitive(mappedDirection))
            } else {
                jsonObject.add("direction_text", JsonPrimitive("UNKNOWN"))
            }

            // Handle potential nulls for strings that should be non-null in Trip model
            val stringFields = arrayOf("terminal", "gate", "description", "departure_text", "route_id")
            for (field in stringFields) {
                if (!jsonObject.has(field) || jsonObject.get(field).isJsonNull) {
                    jsonObject.add(field, JsonPrimitive(""))
                }
            }

            // Handle boolean
            if (!jsonObject.has("actual") || jsonObject.get("actual").isJsonNull) {
                jsonObject.add("actual", JsonPrimitive(false))
            }

            return gson.fromJson(jsonObject, Trip::class.java)
        }
    }
}
