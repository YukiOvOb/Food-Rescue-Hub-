package com.example.foodrescuehub.data.model

/**
 * Consumer profile data class
 * Represents user profile with location information
 */
data class ConsumerProfile(
    val consumerId: Long,
    val email: String,
    val phone: String?,
    val displayName: String,
    val status: String,
    val role: String,
    val defaultLat: Double?,
    val defaultLng: Double?,
    val preferences: Map<String, Any>?
)
