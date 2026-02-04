package com.example.foodrescuehub.data.model

/**
 * Data class representing a store recommendation from ML model
 * Matches the backend StoreRecommendationDTO structure
 */
data class StoreRecommendation(
    val listingId: Long,
    val title: String,
    val originalPrice: Double,
    val rescuePrice: Double,
    val savingsPercentage: Int,
    val pickupStart: String,
    val pickupEnd: String,
    val photoUrl: String?,
    val qtyAvailable: Int,

    // Store information
    val storeId: Long,
    val storeName: String,
    val category: String,
    val addressLine: String,
    val lat: Double?,
    val lng: Double?,

    // Calculated fields
    val distance: Double?,
    val avgRating: Double?,

    // Recommendation context
    val predictedScore: Double,
    val recommendationReason: String?
) {
    /**
     * Get formatted distance string
     */
    fun getDistanceText(): String {
        return distance?.let { String.format("%.1f km", it) } ?: "N/A"
    }

    /**
     * Get formatted rating string
     */
    fun getRatingText(): String {
        return avgRating?.let { String.format("%.1f", it) } ?: "N/A"
    }

    /**
     * Get tag based on recommendation reason
     */
    fun getRecommendationTag(): String {
        return when {
            recommendationReason?.contains("high rating", ignoreCase = true) == true -> "Top Rated"
            recommendationReason?.contains("nearby", ignoreCase = true) == true -> "Nearby"
            recommendationReason?.contains("discount", ignoreCase = true) == true -> "Great Deal"
            savingsPercentage >= 60 -> "Great Deal"
            avgRating != null && avgRating >= 4.5 -> "Top Rated"
            distance != null && distance < 1.0 -> "Nearby"
            else -> "Recommended"
        }
    }
}

/**
 * Data class for banner items
 */
data class BannerItem(
    val imageUrl: String,
    val title: String,
    val subtitle: String? = null
)
