package com.example.foodrescuehub.data.model

import com.google.gson.annotations.SerializedName

data class ListingReview(
    @SerializedName("reviewId")
    val reviewId: Long,

    @SerializedName("orderId")
    val orderId: Long,

    @SerializedName("listingId")
    val listingId: Long,

    @SerializedName("rating")
    val rating: Int,

    @SerializedName("comment")
    val comment: String,

    @SerializedName("createdAt")
    val createdAt: String?,

    @SerializedName("consumerId")
    val consumerId: Long?,

    @SerializedName("consumerDisplayName")
    val consumerDisplayName: String?
)
