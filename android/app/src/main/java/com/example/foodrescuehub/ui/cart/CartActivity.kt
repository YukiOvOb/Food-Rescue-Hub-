package com.example.foodrescuehub.ui.cart

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodrescuehub.data.model.CartItem
import com.example.foodrescuehub.data.repository.CartManager
import com.example.foodrescuehub.databinding.ActivityCartBinding
import com.example.foodrescuehub.ui.checkout.CheckoutActivity

/**
 * Cart Activity - Displays the shopping cart and handles checkout
 */
class CartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCartBinding
    private lateinit var cartAdapter: CartAdapter
    private var activeDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupClickListeners()
        observeCart()
    }

    override fun onResume() {
        super.onResume()
        // Always fetch the latest cart state when returning to this screen
        CartManager.fetchCart()
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            onIncreaseClick = { listingId -> handleIncrease(listingId) },
            onDecreaseClick = { listingId -> handleDecrease(listingId) },
            onRemoveItem = { item -> handleRemoveItem(item) }
        )

        binding.rvCartItems.apply {
            layoutManager = LinearLayoutManager(this@CartActivity)
            adapter = cartAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnCheckout.setOnClickListener { handleCheckout() }
        binding.btnClearCart.setOnClickListener { showClearCartDialog() }
    }

    private fun observeCart() {
        CartManager.cartItems.observe(this) { items ->
            cartAdapter.submitList(items.toList())
            val isEmpty = items.isEmpty()
            binding.tvEmptyCart.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.rvCartItems.visibility = if (isEmpty) View.GONE else View.VISIBLE
            binding.btnCheckout.isEnabled = !isEmpty
            binding.btnClearCart.visibility = if (isEmpty) View.GONE else View.VISIBLE
            if (isEmpty) activeDialog?.dismiss()
        }

        CartManager.totalPrice.observe(this) { total ->
            binding.tvCartSubtotal.text = "$%.2f".format(total)
            binding.tvCartTotal.text = "$%.2f".format(total)
        }

        CartManager.totalSavings.observe(this) { savings ->
            binding.tvCartSavings.text = "-$%.2f".format(savings)
        }
    }

    private fun handleIncrease(listingId: Long) {
        val currentQuantity = CartManager.getItemQuantity(listingId)
        val item = CartManager.cartItems.value?.find { it.listingId == listingId } ?: return
        if (currentQuantity < item.maxQuantity) {
            CartManager.increaseQuantity(listingId)
        } else {
            Toast.makeText(this, "Maximum quantity is ${item.maxQuantity}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleDecrease(listingId: Long) {
        val currentQuantity = CartManager.getItemQuantity(listingId)
        if (currentQuantity > 1) {
            CartManager.decreaseQuantity(listingId)
        } else {
            val item = CartManager.cartItems.value?.find { it.listingId == listingId } ?: return
            showRemoveItemDialog(item)
        }
    }

    private fun handleRemoveItem(item: CartItem) {
        showRemoveItemDialog(item)
    }

    private fun showRemoveItemDialog(item: CartItem) {
        if (isFinishing) return
        activeDialog?.dismiss()
        activeDialog = AlertDialog.Builder(this)
            .setTitle("Remove Item")
            .setMessage("Remove ${item.title} from cart?")
            .setPositiveButton("Remove") { _, _ -> CartManager.removeItem(item.listingId) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showClearCartDialog() {
        if (isFinishing) return
        activeDialog?.dismiss()
        activeDialog = AlertDialog.Builder(this)
            .setTitle("Clear Cart")
            .setMessage("Remove all items from cart?")
            .setPositiveButton("Clear") { _, _ -> CartManager.clearCart() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun handleCheckout() {
        val items = CartManager.cartItems.value
        if (items.isNullOrEmpty()) return

        val firstItem = items.first()
        val total = CartManager.totalPrice.value ?: 0.0

        val intent = Intent(this, CheckoutActivity::class.java).apply {
            putExtra(CheckoutActivity.EXTRA_TOTAL_AMOUNT, total)
            putExtra(CheckoutActivity.EXTRA_STORE_NAME, firstItem.storeName)
            putExtra(CheckoutActivity.EXTRA_PICKUP_START, firstItem.pickupStart)
            putExtra(CheckoutActivity.EXTRA_PICKUP_END, firstItem.pickupEnd)
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        activeDialog?.dismiss()
        super.onDestroy()
    }
}
