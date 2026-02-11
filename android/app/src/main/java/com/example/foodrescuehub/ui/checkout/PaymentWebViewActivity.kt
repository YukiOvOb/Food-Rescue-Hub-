package com.example.foodrescuehub.ui.checkout

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.foodrescuehub.data.api.RetrofitClient.apiService
import com.example.foodrescuehub.data.repository.CartManager
import com.example.foodrescuehub.databinding.ActivityPaymentWebViewBinding
import com.example.foodrescuehub.ui.orders.OrderDetailActivity
import com.example.foodrescuehub.ui.orders.OrdersActivity
import kotlinx.coroutines.launch

/**
 * WebView Activity to host the Stripe Checkout session.
 * Intercepts success/cancel deep links to handle navigation and order status updates.
 */
class PaymentWebViewActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PAYMENT_URL = "extra_payment_url"
        const val EXTRA_ORDER_IDS = "extra_order_ids"
    }

    private lateinit var binding: ActivityPaymentWebViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val paymentUrl = intent.getStringExtra(EXTRA_PAYMENT_URL)
        if (paymentUrl.isNullOrBlank()) {
            finish()
            return
        }

        setupToolbar()
        setupWebView(paymentUrl)
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(url: String) {
        with(binding.webView) {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            
            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    if (!isFinishing) binding.progressBar.visibility = View.VISIBLE
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    if (!isFinishing) binding.progressBar.visibility = View.GONE
                }

                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    val newUrl = request?.url.toString()
                    android.util.Log.d("PaymentWebView", "Intercepting URL: $newUrl")
                    
                    if (newUrl.contains("frhapp://payment/success")) {
                        handlePaymentCompletion(newUrl)
                        return true
                    }
                    
                    if (newUrl.contains("frhapp://payment/cancel")) {
                        finish()
                        return true
                    }
                    
                    return false
                }
            }
            
            loadUrl(url)
        }
    }

    private fun handlePaymentCompletion(successUrl: String) {
        // Parse order IDs from deep link: frhapp://payment/success?order_ids=101,102
        val uri = Uri.parse(successUrl)
        val orderIdsStr = uri.getQueryParameter("order_ids")
        val orderIds = parseOrderIds(orderIdsStr).ifEmpty {
            intent.getLongArrayExtra(EXTRA_ORDER_IDS)?.toList() ?: emptyList()
        }
        
        Log.d("PaymentWebView", "Success! Updating status for orders: $orderIds")

        // 1. Update backend status to PAID for each order
        orderIds.forEach { orderId ->
            updateOrderStatusToPaid(orderId)
        }

        // 2. Clear the cart
        CartManager.clearCart {
            // 3. Navigate directly to the purchased order when available
            val firstOrderId = orderIds.firstOrNull()
            val nextIntent = if (firstOrderId != null) {
                Intent(this, OrderDetailActivity::class.java).apply {
                    putExtra(OrderDetailActivity.EXTRA_ORDER_ID, firstOrderId)
                }
            } else {
                Intent(this, OrdersActivity::class.java)
            }
            nextIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(nextIntent)
            finish()
        }
    }

    private fun parseOrderIds(raw: String?): List<Long> {
        if (raw.isNullOrBlank()) return emptyList()
        return raw.split(",")
            .mapNotNull { it.trim().toLongOrNull() }
    }

    private fun updateOrderStatusToPaid(orderId: Long) {
        lifecycleScope.launch {
            try {
                val response = apiService.updateOrderStatus(orderId, "PAID")
                if (response.isSuccessful) {
                    Log.d("PaymentWebView", "Successfully updated order $orderId to PAID")
                } else {
                    Log.e("PaymentWebView", "Failed to update order $orderId: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("PaymentWebView", "Error updating order $orderId", e)
            }
        }
    }

    override fun onDestroy() {
        binding.webView.let { 
            (it.parent as? ViewGroup)?.removeView(it)
            it.stopLoading()
            it.clearHistory()
            it.removeAllViews()
            it.destroy()
        }
        super.onDestroy()
    }
}
