package com.example.foodrescuehub

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.foodrescuehub.data.api.RetrofitClient.apiService
import com.example.foodrescuehub.databinding.ActivityPaymentResultBinding
import com.example.foodrescuehub.ui.home.HomeActivity
import com.example.foodrescuehub.ui.orders.OrderDetailActivity
import kotlinx.coroutines.launch

/**
 * Activity to handle the result of external payments via deep links
 */
class PaymentResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        handlePaymentResult()
        setupClickListeners()
    }

    private fun handlePaymentResult() {
        val data: Uri? = intent.data
        if (data == null) {
            showError("No payment data received.")
            return
        }

        // Example URL: frhapp://payment/success?order_ids=101,102
        val path = data.path
        val orderIds = data.getQueryParameter("order_ids")

        if (path?.contains("success") == true || data.toString().contains("success")) {
            showSuccess(orderIds)
        } else if (path?.contains("cancel") == true || data.toString().contains("cancel")) {
            showCancelled()
        } else {
            showError("Payment status unknown.")
        }
    }

    private fun showSuccess(orderIds: String?) {
        binding.ivStatusIcon.setImageResource(android.R.drawable.ic_dialog_info)
        binding.ivStatusIcon.setColorFilter(Color.GREEN)
        binding.tvStatus.text = "Payment Successful!"
        binding.tvStatus.setTextColor(Color.GREEN)
        binding.tvOrderDetails.text = if (!orderIds.isNullOrBlank()) "Orders: $orderIds" else "Your order has been placed."

        // Update backend status to PAID for each order
        orderIds?.split(",")?.forEach { idStr ->
            idStr.trim().toLongOrNull()?.let { orderId ->
                updateOrderStatusToPaid(orderId)
            }
        }

        val firstOrderId = orderIds?.split(",")?.firstOrNull()?.trim()?.toLongOrNull()
        if (firstOrderId != null) {
            val intent = Intent(this, OrderDetailActivity::class.java).apply {
                putExtra(OrderDetailActivity.EXTRA_ORDER_ID, firstOrderId)
            }
            startActivity(intent)
            finish()
        }
    }

    private fun updateOrderStatusToPaid(orderId: Long) {
        lifecycleScope.launch {
            try {
                val response = apiService.updateOrderStatus(orderId, "PAID")
                if (response.isSuccessful) {
                    Log.d("PaymentResult", "Successfully updated order $orderId to PAID")
                } else {
                    Log.e("PaymentResult", "Failed to update order $orderId: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("PaymentResult", "Error updating order $orderId", e)
            }
        }
    }

    private fun showCancelled() {
        binding.ivStatusIcon.setImageResource(android.R.drawable.ic_dialog_alert)
        binding.ivStatusIcon.setColorFilter(Color.RED)
        binding.tvStatus.text = "Payment Cancelled"
        binding.tvStatus.setTextColor(Color.RED)
        binding.tvOrderDetails.text = "Your transaction was cancelled. No charges were made."
    }

    private fun showError(message: String) {
        binding.ivStatusIcon.setImageResource(android.R.drawable.ic_dialog_alert)
        binding.ivStatusIcon.setColorFilter(Color.GRAY)
        binding.tvStatus.text = "Error"
        binding.tvOrderDetails.text = message
    }

    private fun setupClickListeners() {
        binding.btnHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}
