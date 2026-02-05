package com.example.foodrescuehub



import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast


import android.net.Uri

import androidx.browser.customtabs.CustomTabsIntent

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import com.example.foodrescuehub.data.api.ApiService
import com.example.foodrescuehub.data.api.RetrofitClient
import com.example.foodrescuehub.data.model.CheckoutRequest
import com.example.foodrescuehub.data.model.CheckoutItem

class CheckoutActivity : AppCompatActivity() {


   val apiService = RetrofitClient.instance.create(ApiService::class.java)

    private fun onPayButtonClicked() {

        val items = listOf(
            CheckoutItem(listingId = 5, quantity = 1),
            CheckoutItem(listingId = 8, quantity = 2)
        )
        val request = CheckoutRequest(userId = 1, items = items)


        CoroutineScope(Dispatchers.Main).launch {
            try {

                val response = apiService.startCheckout(request)

                if (response.isSuccessful && response.body() != null) {
                    val paymentUrl = response.body()!!.paymentUrl

                    // use Chrome Custom Tabs to open payment link

                    val customTabsIntent = CustomTabsIntent.Builder().build()
                    customTabsIntent.launchUrl(this@CheckoutActivity, Uri.parse(paymentUrl))

                } else {
                    Toast.makeText(this@CheckoutActivity, "Failed to create order", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CheckoutActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}