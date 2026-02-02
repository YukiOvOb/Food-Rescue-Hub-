package com.example.foodrescuehub.data.model

/**
 * Data class representing an item in the shopping cart
 */
data class CartItem(
    val listingId: Long,
    val title: String,
    val storeName: String,
    val price: Double,
    val originalPrice: Double,
    val photoUrl: String?,
    var quantity: Int = 1,
    val maxQuantity: Int = 10
) {
    /**
     * Calculate subtotal for this item
     */
    fun getSubtotal(): Double = price * quantity

    /**
     * Calculate total savings for this item
     */
    fun getSavings(): Double = (originalPrice - price) * quantity
}

// AddCartItemRequest.kt
data class AddCartItemRequest(
    val listingId: Long,
    val qty: Int
)

// UpdateCartItemRequest.kt
data class UpdateCartItemRequest(
    val qty: Int
)

// CartResponseDto.kt
data class CartResponseDto(
    val cartId: Long,
    val supplierId: Long?,
    val items: List<CartItemDto>,
    val subtotal: Double,
    val total: Double
)

data class CartItemDto(
    val listingId: Long,
    val title: String,
    val imageUrl: String?,
    val unitPrice: Double,
    val qty: Int,
    val lineTotal: Double
)


