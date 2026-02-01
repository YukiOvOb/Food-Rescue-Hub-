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
    private val _lat: Any?,

    @SerializedName("lng")
    private val _lng: Any?
) {
    val lat: Double?
        get() = when (_lat) {
            is Number -> _lat.toDouble()
            is String -> _lat.toDoubleOrNull()
            else -> null
        }

    val lng: Double?
        get() = when (_lng) {
            is Number -> _lng.toDouble()
            is String -> _lng.toDoubleOrNull()
            else -> null
        }
}

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
    val title: String
)
