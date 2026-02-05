package com.example.foodrescuehub.data.model

data class CheckoutItemDto(
    val listingId: Long,
    val quantity: Int
)

data class CheckoutRequestDto(
    val items: List<CheckoutItemDto>,
    val pickupSlotStart: String,
    val pickupSlotEnd: String
)

data class CheckoutResponseDto(
    val paymentUrl: String,
    val orderIds: List<Long>
)
