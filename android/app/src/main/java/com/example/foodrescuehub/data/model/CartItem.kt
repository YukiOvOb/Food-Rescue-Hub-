package com.example.foodrescuehub.data.model

import com.google.gson.annotations.SerializedName

/**
 * Data class representing an item in the shopping cart
 */
data class CartItem(
    val listingId: Long,
    val title: String,
    val storeName: String,
    val price: Double,
    val originalPrice: Double,
    val savingsLabel: String? = null,
    val photoUrl: String?,
    var quantity: Int = 1,
    val maxQuantity: Int = 10,
    val pickupStart: String? = null,
    val pickupEnd: String? = null
) {
    fun getSubtotal(): Double = price * quantity
    fun getSavings(): Double = (originalPrice - price) * quantity
}

data class AddCartItemRequest(
    val listingId: Long,
    val qty: Int
)

data class UpdateCartItemRequest(
    val qty: Int
)

data class CartResponseDto(
    val cartId: Long,
    val supplierId: Long?,
    val storeName: String?,
    val items: List<CartItemDto>,
    val subtotal: Double,
    val total: Double,
    val totalSavings: Double // Sum of all savings
)

data class CartItemDto(
    val listingId: Long,
    val title: String,
    val storeName: String?,
    val imageUrl: String?,
    val unitPrice: Double,
    val originalPrice: Double,
    val savingsLabel: String?, // New field to match ListingDTO
    val qty: Int,
    val lineTotal: Double,
    @SerializedName("pickupStart")
    val pickupStart: String?,
    @SerializedName("pickupEnd")
    val pickupEnd: String?
)
