package com.example.foodrescuehub.data.model

import com.google.gson.annotations.SerializedName

/**
 * Order data model
 * Represents a customer order in the Food Rescue Hub system
 */
data class Order(
    @SerializedName("orderId")
    val orderId: Long,

    @SerializedName("status")
    val status: String,

    @SerializedName("totalAmount")
    val totalAmount: Double,

    @SerializedName("currency")
    val currency: String = "SGD",

    @SerializedName("pickupSlotStart")
    val pickupSlotStart: String?,

    @SerializedName("pickupSlotEnd")
    val pickupSlotEnd: String?,

    @SerializedName("cancelReason")
    val cancelReason: String?,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String,

    @SerializedName("store")
    val store: OrderStore?,

    @SerializedName("consumer")
    val consumer: OrderConsumer?,

    @SerializedName("orderItems")
    val orderItems: List<OrderItem>? = emptyList()
)

/**
 * Store information in an order
 */
data class OrderStore(
    @SerializedName("storeId")
    val storeId: Long,

    @SerializedName("storeName")
    val storeName: String?,

    @SerializedName("addressLine")
    val addressLine: String?,

    @SerializedName("postalCode")
    val postalCode: String?,

    @SerializedName("lat")
    val lat: Double?,

    @SerializedName("lng")
    val lng: Double?
)

/**
 * Consumer information in an order
 */
data class OrderConsumer(
    @SerializedName("consumerId")
    val consumerId: Long,

    @SerializedName("displayName")
    val displayName: String?,

    @SerializedName("default_lat")
    val defaultLat: Double?,

    @SerializedName("default_lng")
    val defaultLng: Double?
)

/**
 * Order item data model
 */
data class OrderItem(
    @SerializedName("orderItemId")
    val orderItemId: Long,

    @SerializedName("quantity")
    val quantity: Int,

    @SerializedName("unitPrice")
    val unitPrice: Double,

    @SerializedName("lineTotal")
    val lineTotal: Double,

    @SerializedName("listing")
    val listing: OrderListingInfo?
)

/**
 * Listing information in an order item
 */
data class OrderListingInfo(
    @SerializedName("listingId")
    val listingId: Long,

    @SerializedName("title")
    val title: String,

    @SerializedName("hasReviewed")
    val hasReviewed: Boolean = false
)

// CreateOrderResponseDto.kt
data class CreateOrderResponseDto(
    val orderId: Long,
    val totalAmount: Double,
    val pickupToken: String?
)

// CreateOrderRequest.kt
data class CreateOrderRequest(
    val listingId: Long,
    val consumerId: Long,
    val quantity: Int,
    val pickupSlotStart: String? = null,
    val pickupSlotEnd: String? = null
)

// CancelOrderRequest.kt
data class CancelOrderRequest(
    val reason: String
)
