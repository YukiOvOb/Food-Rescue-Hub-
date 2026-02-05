package com.example.foodrescuehub.ui.checkout

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.foodrescuehub.databinding.ActivityPaymentProcessingBinding
import com.example.foodrescuehub.ui.orders.OrdersActivity

/**
 * Landing page shown after user is redirected to the payment gateway.
 * Allows user to manually check status if deep link redirect doesn't trigger.
 */
class PaymentProcessingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentProcessingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentProcessingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCheckStatus.setOnClickListener {
            // Navigate to Order History to see the latest status from backend
            val intent = Intent(this, OrdersActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }
}
