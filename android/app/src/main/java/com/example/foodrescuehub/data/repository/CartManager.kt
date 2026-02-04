package com.example.foodrescuehub.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.foodrescuehub.data.api.RetrofitClient
import com.example.foodrescuehub.data.model.AddCartItemRequest
import com.example.foodrescuehub.data.model.CartItem
import com.example.foodrescuehub.data.model.CartResponseDto
import com.example.foodrescuehub.data.model.UpdateCartItemRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Singleton class to manage shopping cart with backend synchronization
 */
object CartManager {

    private const val TAG = "CartManager"
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _cartItems = MutableLiveData<MutableList<CartItem>>(mutableListOf())
    val cartItems: LiveData<MutableList<CartItem>> = _cartItems

    private val _totalPrice = MutableLiveData<Double>(0.0)
    val totalPrice: LiveData<Double> = _totalPrice

    private val _totalSavings = MutableLiveData<Double>(0.0)
    val totalSavings: LiveData<Double> = _totalSavings

    private val _itemCount = MutableLiveData<Int>(0)
    val itemCount: LiveData<Int> = _itemCount

    private val _supplierId = MutableLiveData<Long?>(null)
    val supplierId: LiveData<Long?> = _supplierId

    /**
     * Fetch the latest cart state from the backend
     */
    fun fetchCart() {
        scope.launch {
            try {
                val response = RetrofitClient.apiService.getCart()
                if (response.isSuccessful) {
                    val cartDto = response.body()
                    withContext(Dispatchers.Main) {
                        updateLocalState(cartDto)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching cart", e)
            }
        }
    }

    /**
     * Add an item to the cart (Backend sync)
     */
    fun addItem(listingId: Long, quantity: Int = 1, onResult: ((Boolean, String?) -> Unit)? = null) {
        scope.launch {
            try {
                val response = RetrofitClient.apiService.addItemToCart(AddCartItemRequest(listingId, quantity))
                if (response.isSuccessful) {
                    val cartDto = response.body()
                    withContext(Dispatchers.Main) {
                        updateLocalState(cartDto)
                        onResult?.invoke(true, null)
                    }
                } else {
                    val errorMsg = if (response.code() == 500) "Cross-store restriction" else "Failed to add item"
                    withContext(Dispatchers.Main) {
                        onResult?.invoke(false, errorMsg)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error adding item", e)
                withContext(Dispatchers.Main) {
                    onResult?.invoke(false, e.message)
                }
            }
        }
    }

    /**
     * Update quantity of an item (Backend sync)
     */
    fun updateQuantity(listingId: Long, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeItem(listingId)
            return
        }

        scope.launch {
            try {
                val response = RetrofitClient.apiService.updateCartItemQuantity(listingId, UpdateCartItemRequest(newQuantity))
                if (response.isSuccessful) {
                    val cartDto = response.body()
                    withContext(Dispatchers.Main) {
                        updateLocalState(cartDto)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating quantity", e)
            }
        }
    }

    fun increaseQuantity(listingId: Long) {
        val currentQty = getItemQuantity(listingId)
        updateQuantity(listingId, currentQty + 1)
    }

    fun decreaseQuantity(listingId: Long) {
        val currentQty = getItemQuantity(listingId)
        if (currentQty > 1) {
            updateQuantity(listingId, currentQty - 1)
        } else {
            removeItem(listingId)
        }
    }

    /**
     * Remove an item from the cart (Backend sync)
     */
    fun removeItem(listingId: Long) {
        scope.launch {
            try {
                val response = RetrofitClient.apiService.removeCartItem(listingId)
                if (response.isSuccessful) {
                    val cartDto = response.body()
                    withContext(Dispatchers.Main) {
                        updateLocalState(cartDto)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error removing item", e)
            }
        }
    }

    /**
     * Clear the entire cart (Backend sync)
     */
    fun clearCart(onResult: ((Boolean) -> Unit)? = null) {
        scope.launch {
            try {
                val response = RetrofitClient.apiService.clearCart()
                if (response.isSuccessful) {
                    val cartDto = response.body()
                    withContext(Dispatchers.Main) {
                        updateLocalState(cartDto)
                        onResult?.invoke(true)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onResult?.invoke(false)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing cart", e)
                withContext(Dispatchers.Main) {
                    onResult?.invoke(false)
                }
            }
        }
    }

    /**
     * Helper to update local LiveData from Backend DTO
     */
    private fun updateLocalState(cartDto: CartResponseDto?) {
        if (cartDto == null) {
            _cartItems.value = mutableListOf()
            _totalPrice.value = 0.0
            _totalSavings.value = 0.0
            _itemCount.value = 0
            _supplierId.value = null
            return
        }

        val cartStoreName = cartDto.storeName ?: ""

        val mappedItems = cartDto.items.map { dto ->
            CartItem(
                listingId = dto.listingId,
                title = dto.title,
                storeName = dto.storeName ?: cartStoreName,
                price = dto.unitPrice,
                originalPrice = dto.originalPrice, 
                savingsLabel = dto.savingsLabel,
                photoUrl = dto.imageUrl,
                quantity = dto.qty,
                pickupStart = dto.pickupStart,
                pickupEnd = dto.pickupEnd
            )
        }.toMutableList()

        _cartItems.value = mappedItems
        _totalPrice.value = cartDto.total
        _totalSavings.value = cartDto.totalSavings // Directly use calculated savings from backend
        _itemCount.value = mappedItems.sumOf { it.quantity }
        _supplierId.value = cartDto.supplierId
    }

    fun getItemCount(): Int = _itemCount.value ?: 0
    fun isInCart(listingId: Long): Boolean = _cartItems.value?.any { it.listingId == listingId } ?: false
    fun getItemQuantity(listingId: Long): Int = _cartItems.value?.find { it.listingId == listingId }?.quantity ?: 0

    internal fun clearCartForLogout() {
        _cartItems.value = mutableListOf()
        _totalPrice.value = 0.0
        _totalSavings.value = 0.0
        _itemCount.value = 0
        _supplierId.value = null
    }
}
