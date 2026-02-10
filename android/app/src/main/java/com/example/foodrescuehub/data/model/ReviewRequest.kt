package com.example.foodrescuehub.data.model

data class ReviewRequest(
    val orderId: Long,
    val listingId: Long,
    val rating: Int,
    val comment: String
)
