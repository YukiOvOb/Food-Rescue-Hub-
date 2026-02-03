package com.example.foodrescuehub.ui.checkout

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.foodrescuehub.data.api.RetrofitClient.apiService
import com.example.foodrescuehub.data.repository.CartManager
import com.example.foodrescuehub.data.storage.SecurePreferences
import com.example.foodrescuehub.databinding.ActivityCheckoutBinding
import com.example.foodrescuehub.ui.orders.OrderDetailActivity
import com.example.foodrescuehub.ui.orders.OrderDetailActivity.Companion.EXTRA_CONSUMER_ID
import com.example.foodrescuehub.ui.orders.OrderDetailActivity.Companion.EXTRA_ORDER_ID
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Checkout Activity - Handles order confirmation and pickup time selection
 */
class CheckoutActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TOTAL_AMOUNT = "extra_total_amount"
        const val EXTRA_STORE_NAME = "extra_store_name"
        const val EXTRA_PICKUP_START = "extra_pickup_start"
        const val EXTRA_PICKUP_END = "extra_pickup_end"
    }

    private lateinit var binding: ActivityCheckoutBinding
    private lateinit var securePreferences: SecurePreferences
    
    private var selectedStartTime: Calendar? = null
    private var selectedEndTime: Calendar? = null
    
    private var allowedStartTime: Date? = null
    private var allowedEndTime: Date? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        securePreferences = SecurePreferences(this)
        
        parsePickupWindow()
        setupToolbar()
        displayOrderSummary()
        setupClickListeners()
    }

    private fun parsePickupWindow() {
        val startStr = intent.getStringExtra(EXTRA_PICKUP_START)
        val endStr = intent.getStringExtra(EXTRA_PICKUP_END)
        
        val format1 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val format2 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
        
        try {
            if (startStr != null) {
                allowedStartTime = try { format1.parse(startStr) } catch (e: Exception) { format2.parse(startStr) }
            }
            if (endStr != null) {
                allowedEndTime = try { format1.parse(endStr) } catch (e: Exception) { format2.parse(endStr) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun displayOrderSummary() {
        val total = intent.getDoubleExtra(EXTRA_TOTAL_AMOUNT, 0.0)
        val storeName = intent.getStringExtra(EXTRA_STORE_NAME) ?: "Store"
        val itemCount = CartManager.getItemCount()

        binding.tvTotalAmount.text = "$%.2f".format(total)
        binding.tvStoreName.text = storeName
        binding.tvItemCount.text = "$itemCount items"
        
        if (allowedStartTime != null && allowedEndTime != null) {
            val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
            val windowText = "${sdf.format(allowedStartTime!!)} - ${sdf.format(allowedEndTime!!)}"
            binding.tvAllowedWindow.text = "Available Pickup Window: $windowText"
        } else {
            binding.tvAllowedWindow.text = "Please select a pickup time"
        }
    }

    private fun setupClickListeners() {
        binding.btnSelectTime.setOnClickListener {
            showTimePickerDialog()
        }

        binding.btnPlaceOrder.setOnClickListener {
            placeOrder()
        }
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val now = calendar.time
        
        allowedStartTime?.let { start ->
            if (start.after(now)) {
                calendar.time = start
            }
        }
        
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            val start = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, selectedHour)
                set(Calendar.MINUTE, selectedMinute)
                set(Calendar.SECOND, 0)
            }
            
            if (isTimeInWindow(start.time)) {
                val end = (start.clone() as Calendar).apply {
                    add(Calendar.HOUR_OF_DAY, 1)
                    if (allowedEndTime != null && time.after(allowedEndTime)) {
                        time = allowedEndTime!!
                    }
                }

                selectedStartTime = start
                selectedEndTime = end

                val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
                val timeRange = "${sdf.format(start.time)} - ${sdf.format(end.time)}"
                
                binding.tvSelectedTime.text = "Your Selected Slot: $timeRange"
                binding.tvSelectedTime.setTextColor(getColor(android.R.color.black))
                binding.tvSelectedTime.visibility = View.VISIBLE
                binding.btnSelectTime.text = "Change Time Slot"
                binding.btnPlaceOrder.isEnabled = true
            } else {
                val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
                val windowText = if (allowedStartTime != null && allowedEndTime != null) {
                    "${sdf.format(allowedStartTime!!)} and ${sdf.format(allowedEndTime!!)}"
                } else "the pickup window"
                
                Toast.makeText(this, "Store is only open for pickup between $windowText", Toast.LENGTH_LONG).show()
                binding.btnPlaceOrder.isEnabled = false
                binding.tvSelectedTime.text = "Invalid selection"
                binding.tvSelectedTime.setTextColor(getColor(android.R.color.holo_red_dark))
                binding.tvSelectedTime.visibility = View.VISIBLE
            }
        }, hour, minute, false).show()
    }

    private fun isTimeInWindow(date: Date): Boolean {
        if (allowedStartTime == null || allowedEndTime == null) return true

        val selectedCal = Calendar.getInstance().apply { time = date }
        val startCal = Calendar.getInstance().apply { time = allowedStartTime!! }
        val endCal = Calendar.getInstance().apply { time = allowedEndTime!! }

        val selectedMinutes = selectedCal.get(Calendar.HOUR_OF_DAY) * 60 + selectedCal.get(Calendar.MINUTE)
        val startMinutes = startCal.get(Calendar.HOUR_OF_DAY) * 60 + startCal.get(Calendar.MINUTE)
        val endMinutes = endCal.get(Calendar.HOUR_OF_DAY) * 60 + endCal.get(Calendar.MINUTE)

        return if (startMinutes > endMinutes) {
            selectedMinutes >= startMinutes || selectedMinutes <= endMinutes
        } else {
            selectedMinutes in startMinutes..endMinutes
        }
    }

    private fun placeOrder() {
        val start = selectedStartTime ?: return
        val end = selectedEndTime ?: return

        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val startTimeStr = isoFormat.format(start.time)
        val endTimeStr = isoFormat.format(end.time)

        binding.loadingOverlay.visibility = View.VISIBLE
        binding.btnPlaceOrder.isEnabled = false

        lifecycleScope.launch {
            try {
                val response = apiService.createOrder(startTimeStr, endTimeStr)

                if (response.isSuccessful && response.body() != null) {
                    val orderResponse = response.body()!!
                    Toast.makeText(this@CheckoutActivity, "Order placed successfully!", Toast.LENGTH_LONG).show()
                    CartManager.clearCart()
                    
                    val consumerId = securePreferences.getUserId()
                    val intent = Intent(this@CheckoutActivity, OrderDetailActivity::class.java).apply {
                        putExtra(EXTRA_ORDER_ID, orderResponse.orderId)
                        putExtra(EXTRA_CONSUMER_ID, consumerId)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish()
                } else if (response.code() == 409) {
                    // Handle stock conflict
                    Toast.makeText(this@CheckoutActivity, "Some items just sold out. Your cart has been updated.", Toast.LENGTH_LONG).show()
                    CartManager.fetchCart() // Refresh cart data
                    finish() // Go back to cart to see changes
                } else {
                    Toast.makeText(this@CheckoutActivity, "Failed to place order: ${response.message()}", Toast.LENGTH_SHORT).show()
                    binding.btnPlaceOrder.isEnabled = true
                }
            } catch (e: Exception) {
                Toast.makeText(this@CheckoutActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.btnPlaceOrder.isEnabled = true
            } finally {
                binding.loadingOverlay.visibility = View.GONE
            }
        }
    }
}
