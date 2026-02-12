package com.example.foodrescuehub.data.model

/**
 * Data class for login request to the backend
 */
data class LoginRequest(
    val email: String,
    val password: String,
    val role: String
)
