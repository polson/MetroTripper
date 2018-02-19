package com.philsoft.metrotripper.app.nextrip

import com.google.gson.*
import com.philsoft.metrotripper.model.Trip
import com.philsoft.metrotripper.utils.EZ
import java.lang.reflect.Type


object NexTripGsonFactory {
    fun getGson(): Gson {
        return GsonBuilder()
                .registerTypeAdapter(Trip::class.java, deserializer)
                .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                .create()
    }

    private var deserializer = object : JsonDeserializer<Trip> {
        val gson = GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                .create()

        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Trip {
            if (json.asJsonObject.get("DepartureTime") != null) {
                val date = EZ.parseLocationTime(json.asJsonObject.get("DepartureTime").asString)
                json.asJsonObject.add("DepartureTime", JsonPrimitive(date))
            }
            return gson.fromJson(json.asJsonObject, Trip::class.java)
        }
    }


}