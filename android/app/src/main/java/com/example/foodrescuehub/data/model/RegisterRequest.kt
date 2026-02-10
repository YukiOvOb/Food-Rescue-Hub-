package com.example.foodrescuehub.data.model

/**
 * Registration payload matching backend RegisterRequest
 */
data class RegisterRequest(
    val email: String,
    val password: String,
    val displayName: String,
    val phone: String? = null,
    val businessName: String? = null,
    val businessType: String? = null,
    val payoutAccountRef: String? = null,
    val role: String = "CONSUMER"
)
