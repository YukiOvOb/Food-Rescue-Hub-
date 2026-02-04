package com.example.foodrescuehub.data.model

/**
 * Response wrapper for recommendation API
 * Matches the backend API response structure
 */
data class RecommendationResponse(
    val success: Boolean,
    val consumerId: Long,
    val count: Int,
    val message: String,
    val recommendations: List<StoreRecommendation>
)
