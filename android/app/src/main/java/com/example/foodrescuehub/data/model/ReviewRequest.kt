package com.example.foodrescuehub.data.model

data class ReviewRequest(
    val orderId: Long,
    val listingId: Long,
    val storeRating: Int,
    val listingAccuracy: Int,
    val onTimePickup: Int,
    val comment: String
)
