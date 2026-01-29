package com.example.foodrescuehub.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.foodrescuehub.data.model.CartItem

/**
 * Singleton class to manage shopping cart
 */
object CartManager {

    private val _cartItems = MutableLiveData<MutableList<CartItem>>(mutableListOf())
    val cartItems: LiveData<MutableList<CartItem>> = _cartItems

    private val _totalPrice = MutableLiveData<Double>(0.0)
    val totalPrice: LiveData<Double> = _totalPrice

    private val _totalSavings = MutableLiveData<Double>(0.0)
    val totalSavings: LiveData<Double> = _totalSavings

    private val _itemCount = MutableLiveData<Int>(0)
    val itemCount: LiveData<Int> = _itemCount

    fun addItem(item: CartItem) {
        val currentItems = _cartItems.value ?: mutableListOf()
        val itemIndex = currentItems.indexOfFirst { it.listingId == item.listingId }

        if (itemIndex != -1) {
            val existingItem = currentItems[itemIndex]
            if (existingItem.quantity < existingItem.maxQuantity) {
                // Create a new CartItem with increased quantity (deep copy)
                val updatedItem = existingItem.copy(quantity = existingItem.quantity + 1)
                currentItems[itemIndex] = updatedItem
            }
        } else {
            currentItems.add(item)
        }

        // Create a new list to trigger LiveData and DiffUtil properly
        _cartItems.value = currentItems.toMutableList()
        updateTotals()
    }

    fun removeItem(listingId: Long) {
        val currentItems = _cartItems.value ?: mutableListOf()
        currentItems.removeAll { it.listingId == listingId }
        // Create a new list to trigger LiveData update properly
        _cartItems.value = currentItems.toMutableList()
        updateTotals()
    }

    fun updateQuantity(listingId: Long, newQuantity: Int) {
        val currentItems = _cartItems.value ?: mutableListOf()
        val itemIndex = currentItems.indexOfFirst { it.listingId == listingId }

        if (itemIndex != -1) {
            val item = currentItems[itemIndex]
            if (newQuantity > 0 && newQuantity <= item.maxQuantity) {
                // Create a new CartItem with updated quantity (deep copy)
                val updatedItem = item.copy(quantity = newQuantity)
                currentItems[itemIndex] = updatedItem
                // Create a new list to trigger LiveData and DiffUtil properly
                _cartItems.value = currentItems.toMutableList()
                updateTotals()
            } else if (newQuantity <= 0) {
                removeItem(listingId)
            }
        }
    }

    fun increaseQuantity(listingId: Long) {
        val currentItems = _cartItems.value ?: mutableListOf()
        val itemIndex = currentItems.indexOfFirst { it.listingId == listingId }

        if (itemIndex != -1) {
            val item = currentItems[itemIndex]
            if (item.quantity < item.maxQuantity) {
                // Create a new CartItem with increased quantity (deep copy)
                val updatedItem = item.copy(quantity = item.quantity + 1)
                currentItems[itemIndex] = updatedItem
                // Create a new list to trigger LiveData and DiffUtil properly
                _cartItems.value = currentItems.toMutableList()
                updateTotals()
            }
        }
    }

    fun decreaseQuantity(listingId: Long) {
        val currentItems = _cartItems.value ?: mutableListOf()
        val itemIndex = currentItems.indexOfFirst { it.listingId == listingId }

        if (itemIndex != -1) {
            val item = currentItems[itemIndex]
            if (item.quantity > 1) {
                // Create a new CartItem with decreased quantity (deep copy)
                val updatedItem = item.copy(quantity = item.quantity - 1)
                currentItems[itemIndex] = updatedItem
                // Create a new list to trigger LiveData and DiffUtil properly
                _cartItems.value = currentItems.toMutableList()
                updateTotals()
            } else {
                removeItem(listingId)
            }
        }
    }

    fun clearCart() {
        _cartItems.value = mutableListOf()
        updateTotals()
    }

    fun getItemCount(): Int {
        return _cartItems.value?.sumOf { it.quantity } ?: 0
    }

    fun isInCart(listingId: Long): Boolean {
        return _cartItems.value?.any { it.listingId == listingId } ?: false
    }

    fun getItemQuantity(listingId: Long): Int {
        return _cartItems.value?.find { it.listingId == listingId }?.quantity ?: 0
    }

    private fun updateTotals() {
        val items = _cartItems.value ?: mutableListOf()
        _totalPrice.value = items.sumOf { it.getSubtotal() }
        _totalSavings.value = items.sumOf { it.getSavings() }
        _itemCount.value = items.sumOf { it.quantity }
    }
}
