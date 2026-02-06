package com.example.foodrescuehub.ui.checkout

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodrescuehub.data.api.RetrofitClient
import com.example.foodrescuehub.data.model.CheckoutItemDto
import com.example.foodrescuehub.data.model.CheckoutRequestDto
import com.example.foodrescuehub.data.model.CheckoutResponseDto
import com.example.foodrescuehub.data.repository.CartManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

sealed class CheckoutState {
    object Idle : CheckoutState()
    object Loading : CheckoutState()
    data class Success(val paymentUrl: String, val orderIds: List<Long>) : CheckoutState()
    data class Error(val message: String) : CheckoutState()
    data class Conflict(val message: String) : CheckoutState()
    object Unauthorized : CheckoutState()
}

class CheckoutViewModel : ViewModel() {

    private val _state = MutableLiveData<CheckoutState>(CheckoutState.Idle)
    val state: LiveData<CheckoutState> = _state

    private val _pickupSlotStart = MutableLiveData<Calendar?>()
    val pickupSlotStart: LiveData<Calendar?> = _pickupSlotStart

    private val _pickupSlotEnd = MutableLiveData<Calendar?>()
    val pickupSlotEnd: LiveData<Calendar?> = _pickupSlotEnd

    fun setPickupSlot(start: Calendar, end: Calendar) {
        _pickupSlotStart.value = start
        _pickupSlotEnd.value = end
    }

    fun startCheckout() {
        val start = _pickupSlotStart.value
        val end = _pickupSlotEnd.value

        if (start == null || end == null) {
            _state.value = CheckoutState.Error("Please select a pickup time slot.")
            return
        }

        // Validate range (max 6 hours)
        val diffHours = (end.timeInMillis - start.timeInMillis) / (1000 * 60 * 60)
        if (diffHours > 6 || end.before(start)) {
            _state.value = CheckoutState.Error("Invalid pickup window. Max 6 hours allowed.")
            return
        }

        val cartItems = CartManager.cartItems.value ?: emptyList()
        if (cartItems.isEmpty()) {
            _state.value = CheckoutState.Error("Your cart is empty.")
            return
        }

        val checkoutItems = cartItems.map { CheckoutItemDto(it.listingId, it.quantity) }
        
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("Asia/Singapore")
        }
        
        val request = CheckoutRequestDto(
            items = checkoutItems,
            pickupSlotStart = isoFormat.format(start.time),
            pickupSlotEnd = isoFormat.format(end.time)
        )

        viewModelScope.launch {
            _state.value = CheckoutState.Loading
            try {
                val response = RetrofitClient.apiService.startCheckout(request)
                when (response.code()) {
                    200 -> {
                        val body = response.body()
                        if (body != null) {
                            _state.value = CheckoutState.Success(body.paymentUrl, body.orderIds)
                        } else {
                            _state.value = CheckoutState.Error("Empty response from server")
                        }
                    }
                    401 -> {
                        _state.value = CheckoutState.Unauthorized
                    }
                    409 -> {
                        _state.value = CheckoutState.Conflict("Items sold out or insufficient stock. Cart refreshed.")
                        CartManager.fetchCart()
                    }
                    400 -> {
                        _state.value = CheckoutState.Error(response.message() ?: "Bad Request")
                    }
                    else -> {
                        _state.value = CheckoutState.Error("Server error: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                _state.value = CheckoutState.Error("Network error: ${e.message}")
            }
        }
    }

    fun resetState() {
        _state.value = CheckoutState.Idle
    }
}
