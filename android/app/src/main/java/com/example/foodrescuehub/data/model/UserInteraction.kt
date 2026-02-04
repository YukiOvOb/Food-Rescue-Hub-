package com.example.foodrescuehub.data.model

/**
 * Data class for recording user interactions
 */
data class UserInteractionRequest(
    val consumerId: Long,
    val listingId: Long,
    val interactionType: String,  // "VIEW", "CLICK", "SEARCH", "ADD_TO_CART"
    val sessionId: String? = null,
    val deviceType: String = "Android"
)

data class InteractionResponse(
    val success: Boolean,
    val interactionId: Long? = null,
    val message: String? = null,
    val error: String? = null
)
