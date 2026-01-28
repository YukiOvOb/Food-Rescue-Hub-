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
        val existingItem = currentItems.find { it.listingId == item.listingId }

        if (existingItem != null) {
            if (existingItem.quantity < existingItem.maxQuantity) {
                existingItem.quantity++
            }
        } else {
            currentItems.add(item)
        }

        _cartItems.value = currentItems
        updateTotals()
    }

    fun removeItem(listingId: Long) {
        val currentItems = _cartItems.value ?: mutableListOf()
        currentItems.removeAll { it.listingId == listingId }
        _cartItems.value = currentItems
        updateTotals()
    }

    fun updateQuantity(listingId: Long, newQuantity: Int) {
        val currentItems = _cartItems.value ?: mutableListOf()
        val item = currentItems.find { it.listingId == listingId }

        item?.let {
            if (newQuantity > 0 && newQuantity <= it.maxQuantity) {
                it.quantity = newQuantity
                _cartItems.value = currentItems
                updateTotals()
            } else if (newQuantity <= 0) {
                removeItem(listingId)
            }
        }
    }

    fun increaseQuantity(listingId: Long) {
        val currentItems = _cartItems.value ?: mutableListOf()
        val item = currentItems.find { it.listingId == listingId }

        item?.let {
            if (it.quantity < it.maxQuantity) {
                it.quantity++
                _cartItems.value = currentItems
                updateTotals()
            }
        }
    }

    fun decreaseQuantity(listingId: Long) {
        val currentItems = _cartItems.value ?: mutableListOf()
        val item = currentItems.find { it.listingId == listingId }

        item?.let {
            if (it.quantity > 1) {
                it.quantity--
                _cartItems.value = currentItems
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
