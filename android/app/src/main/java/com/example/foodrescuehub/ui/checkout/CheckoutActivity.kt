package com.example.foodrescuehub.ui.checkout

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.foodrescuehub.databinding.ActivityCheckoutBinding
import com.example.foodrescuehub.ui.auth.LoginActivity
import com.example.foodrescuehub.data.repository.AuthManager
import java.text.SimpleDateFormat
import java.util.*

/**
 * Checkout Activity - Handles pickup selection and starts Stripe payment flow via WebView.
 */
class CheckoutActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TOTAL_AMOUNT = "extra_total_amount"
        const val EXTRA_STORE_NAME = "extra_store_name"
        const val EXTRA_PICKUP_START = "extra_pickup_start"
        const val EXTRA_PICKUP_END = "extra_pickup_end"
    }

    private lateinit var binding: ActivityCheckoutBinding
    private val viewModel: CheckoutViewModel by viewModels()

    private var allowedStartTime: Calendar? = null
    private var allowedEndTime: Calendar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        parsePickupConstraints()
        setupToolbar()
        displayOrderSummary()
        setupClickListeners()
        observeViewModel()
    }

    private fun parsePickupConstraints() {
        val startStr = intent.getStringExtra(EXTRA_PICKUP_START)
        val endStr = intent.getStringExtra(EXTRA_PICKUP_END)
        
        val formats = listOf(
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
        )

        fun parse(str: String?): Calendar? {
            if (str == null) return null
            for (fmt in formats) {
                try {
                    val date = fmt.parse(str) ?: continue
                    return Calendar.getInstance().apply { time = date }
                } catch (e: Exception) { continue }
            }
            return null
        }

        allowedStartTime = parse(startStr)
        allowedEndTime = parse(endStr)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun displayOrderSummary() {
        val total = intent.getDoubleExtra(EXTRA_TOTAL_AMOUNT, 0.0)
        val storeName = intent.getStringExtra(EXTRA_STORE_NAME) ?: "Store"

        binding.tvTotalAmount.text = "$%.2f".format(total)
        binding.tvStoreName.text = storeName
        
        allowedStartTime?.let { start ->
            allowedEndTime?.let { end ->
                val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
                binding.tvAllowedWindow.text = "Available Pickup: ${sdf.format(start.time)} - ${sdf.format(end.time)}"
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnSelectTime.setOnClickListener { showTimePickerDialog() }
        binding.btnPlaceOrder.setOnClickListener { 
            binding.btnPlaceOrder.isEnabled = false
            viewModel.startCheckout() 
        }
    }

    private fun showTimePickerDialog() {
        val now = Calendar.getInstance()
        val initial = allowedStartTime?.let { if (it.after(now)) it else now } ?: now
        
        TimePickerDialog(this, { _, hour, minute ->
            val selected = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
            }
            
            val end = (selected.clone() as Calendar).apply { add(Calendar.HOUR_OF_DAY, 1) }
            allowedEndTime?.let { if (end.after(it)) end.time = it.time }

            viewModel.setPickupSlot(selected, end)
            
            val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
            binding.tvSelectedTime.text = "Selected Slot: ${sdf.format(selected.time)} - ${sdf.format(end.time)}"
            binding.tvSelectedTime.visibility = View.VISIBLE
            binding.btnPlaceOrder.isEnabled = true
        }, initial.get(Calendar.HOUR_OF_DAY), initial.get(Calendar.MINUTE), false).show()
    }

    private fun observeViewModel() {
        viewModel.state.observe(this) { state ->
            when (state) {
                is CheckoutState.Loading -> {
                    binding.loadingOverlay.visibility = View.VISIBLE
                    binding.btnPlaceOrder.isEnabled = false
                }
                is CheckoutState.Success -> {
                    binding.loadingOverlay.visibility = View.GONE
                    launchPaymentWebView(state.paymentUrl, state.orderIds)
                }
                is CheckoutState.Conflict -> {
                    binding.loadingOverlay.visibility = View.GONE
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                    finish()
                }
                is CheckoutState.Error -> {
                    binding.loadingOverlay.visibility = View.GONE
                    binding.btnPlaceOrder.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
                is CheckoutState.Unauthorized -> {
                    Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show()
                    AuthManager.logout()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finishAffinity()
                }
                else -> {
                    binding.loadingOverlay.visibility = View.GONE
                }
            }
        }
    }

    private fun launchPaymentWebView(url: String, orderIds: List<Long>) {
        val intent = Intent(this, PaymentWebViewActivity::class.java).apply {
            putExtra(PaymentWebViewActivity.EXTRA_PAYMENT_URL, url)
            putExtra(PaymentWebViewActivity.EXTRA_ORDER_IDS, orderIds.toLongArray())
        }
        startActivity(intent)
        finish()
    }
}
