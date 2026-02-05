package com.example.foodrescuehub.ui.checkout

import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.lifecycleScope
import com.example.foodrescuehub.data.api.RetrofitClient.apiService
import com.example.foodrescuehub.data.model.CheckoutItem
import com.example.foodrescuehub.data.model.CheckoutRequest
import com.example.foodrescuehub.data.repository.CartManager
import com.example.foodrescuehub.data.storage.SecurePreferences
import com.example.foodrescuehub.databinding.ActivityCheckoutBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Checkout Activity - Handles order confirmation, pickup selection, and external payment
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
        binding.btnSelectTime.setOnClickListener { showTimePickerDialog() }
        binding.btnPlaceOrder.setOnClickListener { placeOrder() }
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val now = calendar.time
        allowedStartTime?.let { if (it.after(now)) calendar.time = it }

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
                    if (allowedEndTime != null && time.after(allowedEndTime)) time = allowedEndTime!!
                }
                selectedStartTime = start
                selectedEndTime = end
                val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
                binding.tvSelectedTime.text = "Your Selected Slot: ${sdf.format(start.time)} - ${sdf.format(end.time)}"
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
        return if (startMinutes > endMinutes) selectedMinutes >= startMinutes || selectedMinutes <= endMinutes
        else selectedMinutes in startMinutes..endMinutes
    }

    private fun placeOrder() {
        if (selectedStartTime == null || selectedEndTime == null) return

        binding.loadingOverlay.visibility = View.VISIBLE
        binding.btnPlaceOrder.isEnabled = false

        lifecycleScope.launch {
            try {
                // 1. Create Checkout Request from Cart
                val cartItems = CartManager.cartItems.value ?: emptyList()
                val checkoutItems = cartItems.map { CheckoutItem(it.listingId, it.quantity) }
                
                // RETRIEVE User ID from SecurePreferences
                val userId = securePreferences.getUserId()
                
                if (userId == 0L) {
                    Toast.makeText(this@CheckoutActivity, "Error: User not logged in", Toast.LENGTH_SHORT).show()
                    binding.loadingOverlay.visibility = View.GONE
                    binding.btnPlaceOrder.isEnabled = true
                    return@launch
                }

                // 2. Start Payment Process via Team's Endpoint
                val checkoutRequest = CheckoutRequest(userId = userId, items = checkoutItems)
                val response = apiService.startCheckout(checkoutRequest)

                if (response.isSuccessful && response.body() != null) {
                    val paymentUrl = response.body()!!.paymentUrl

                    // 3. Launch external payment browser
                    val customTabsIntent = CustomTabsIntent.Builder().build()
                    customTabsIntent.launchUrl(this@CheckoutActivity, Uri.parse(paymentUrl))

                    // Close this activity as the PaymentResultActivity will handle the deep link return
                    finish()
                } else if (response.code() == 409) {
                    Toast.makeText(this@CheckoutActivity, "Conflict: Items sold out. Refreshing cart.", Toast.LENGTH_LONG).show()
                    CartManager.fetchCart()
                    finish()
                } else {
                    Toast.makeText(this@CheckoutActivity, "Checkout failed: ${response.message()}", Toast.LENGTH_SHORT).show()
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
