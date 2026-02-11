package com.example.foodrescuehub.ui.orders

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodrescuehub.R
import com.example.foodrescuehub.data.api.RetrofitClient
import com.example.foodrescuehub.databinding.ActivityOrdersBinding
import kotlinx.coroutines.launch

/**
 * Orders Activity - Display consumer's order history
 */
class OrdersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrdersBinding
    private lateinit var ordersAdapter: OrdersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupBottomNavigation()
        loadOrders()
    }

    private fun setupToolbar() {
        binding.btnRefresh.setOnClickListener {
            loadOrders()
            Toast.makeText(this, "Refreshing orders...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_orders
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, com.example.foodrescuehub.ui.home.HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_location -> {
                    startActivity(Intent(this, com.example.foodrescuehub.ui.location.LocationActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_orders -> true  // Already on this page
                R.id.nav_profile -> {
                    startActivity(Intent(this, com.example.foodrescuehub.ui.profile.ProfileActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        ordersAdapter = OrdersAdapter { order ->
            val intent = Intent(this, OrderDetailActivity::class.java).apply {
                putExtra(OrderDetailActivity.EXTRA_ORDER_ID, order.orderId)
            }
            startActivity(intent)
        }
        binding.rvOrders.apply {
            layoutManager = LinearLayoutManager(this@OrdersActivity)
            adapter = ordersAdapter
            setHasFixedSize(true)
        }
    }

    private fun loadOrders() {
        showLoading(true)

        lifecycleScope.launch {
            try {
                //Use getMyOrders() which is session-based
                val response = RetrofitClient.apiService.getMyOrders()

                if (response.isSuccessful) {
                    val orders = response.body() ?: emptyList()

                    if (orders.isEmpty()) {
                        showEmptyState()
                    } else {
                        // Sort newest first by created time; tie-break on orderId.
                        val sortedOrders = orders.sortedWith(
                            compareByDescending<com.example.foodrescuehub.data.model.Order> { it.createdAt }
                                .thenByDescending { it.orderId }
                        )
                        ordersAdapter.submitList(sortedOrders)
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
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showOrders() {
        binding.rvOrders.visibility = View.VISIBLE
        binding.llEmptyState.visibility = View.GONE
    }

    private fun showEmptyState() {
        binding.rvOrders.visibility = View.GONE
        binding.llEmptyState.visibility = View.VISIBLE
    }
}
