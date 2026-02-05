package com.example.foodrescuehub

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class PaymentResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_result)

        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val btnHome = findViewById<Button>(R.id.btnHome)


        val data: Uri? = intent.data

        if (data != null && data.toString().contains("success")) {
            // successful
            val orderIds = data.getQueryParameter("order_ids") // 拿到订单号 "101,102"

            tvStatus.text = "Payment Successful!\nOrders: $orderIds"
            tvStatus.setTextColor(Color.GREEN)

            // confirm the status of order

        } else if (data != null && data.toString().contains("cancel")) {
            // cancel
            tvStatus.text = "Payment Cancelled"
            tvStatus.setTextColor(Color.RED)
        }

        btnHome.setOnClickListener {
            // 回到首页
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
    }
}