package com.example.foodrescuehub.data.model

data class ListingReviewResponse(
    val reviewId: Long,
    val orderId: Long,
    val listingId: Long,
    val listingTitle: String?,
    val storeRating: Int,
    val listingAccuracy: Int,
    val onTimePickup: Int,
    val comment: String,
    val createdAt: String,
    val consumerId: Long,
    val consumerDisplayName: String
)
