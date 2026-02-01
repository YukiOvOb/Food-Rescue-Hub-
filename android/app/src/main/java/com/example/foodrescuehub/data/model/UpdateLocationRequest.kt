package com.example.foodrescuehub.data.model

import com.google.gson.annotations.SerializedName

/**
 * Data class for updating user location
 */
data class UpdateLocationRequest(
    @SerializedName("latitude")
    val latitude: Double,

    @SerializedName("longitude")
    val longitude: Double
)
