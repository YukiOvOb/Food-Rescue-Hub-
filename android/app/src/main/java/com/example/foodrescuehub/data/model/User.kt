package com.example.foodrescuehub.data.model

data class User(
    val userId: Long,
    val email: String,
    val displayName: String,
    val phone: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
