package com.example.foodrescuehub.data.model

data class CheckoutRequest(
    val userId: Long,
    val items: List<CheckoutItem>
)

data class CheckoutItem(
    val listingId: Long,
    val quantity: Int
)

// 后端返回的响应
data class CheckoutResponse(
    val paymentUrl: String,
    val orderIds: List<Long>
)