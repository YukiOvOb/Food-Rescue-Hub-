package com.example.foodrescuehub.ui.orders

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodrescuehub.R
import com.example.foodrescuehub.data.api.RetrofitClient
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch

/**
 * Orders Activity - Display consumer's order history
 */
class OrdersActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var rvOrders: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var llEmptyState: LinearLayout
    private lateinit var ordersAdapter: OrdersAdapter

    // TODO: Replace with actual consumer ID from AuthManager
    private val consumerId: Long = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orders)

        initViews()
        setupToolbar()
        setupRecyclerView()
        loadOrders()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        rvOrders = findViewById(R.id.rvOrders)
        progressBar = findViewById(R.id.progressBar)
        llEmptyState = findViewById(R.id.llEmptyState)
    }

    private fun setupToolbar() {
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        ordersAdapter = OrdersAdapter { order ->
            val intent = Intent(this, OrderDetailActivity::class.java).apply {
                putExtra(OrderDetailActivity.EXTRA_ORDER_ID, order.orderId)
                putExtra(OrderDetailActivity.EXTRA_CONSUMER_ID, consumerId)
            }
            startActivity(intent)
        }
        rvOrders.apply {
            layoutManager = LinearLayoutManager(this@OrdersActivity)
            adapter = ordersAdapter
            setHasFixedSize(true)
        }
    }

    private fun loadOrders() {
        showLoading(true)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getConsumerOrders(consumerId)

                if (response.isSuccessful) {
                    val orders = response.body() ?: emptyList()

                    if (orders.isEmpty()) {
                        showEmptyState()
                    } else {
                        ordersAdapter.submitList(orders)
                        showOrders()
                    }
                } else {
                    Toast.makeText(
                        this@OrdersActivity,
                        "Failed to load orders: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                    showEmptyState()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@OrdersActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                showEmptyState()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showOrders() {
        rvOrders.visibility = View.VISIBLE
        llEmptyState.visibility = View.GONE
    }

    private fun showEmptyState() {
        rvOrders.visibility = View.GONE
        llEmptyState.visibility = View.VISIBLE
    }
}
