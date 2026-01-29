package com.example.foodrescuehub.ui.cart

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodrescuehub.R
import com.example.foodrescuehub.data.model.CartItem
import com.example.foodrescuehub.data.repository.CartManager

class CartActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var rvCartItems: RecyclerView
    private lateinit var tvEmptyCart: View
    private lateinit var tvSubtotal: TextView
    private lateinit var tvSavings: TextView
    private lateinit var tvTotal: TextView
    private lateinit var btnCheckout: Button
    private lateinit var btnClearCart: Button
    private lateinit var cartAdapter: CartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        initViews()
        setupRecyclerView()
        setupClickListeners()
        observeCart()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        rvCartItems = findViewById(R.id.rvCartItems)
        tvEmptyCart = findViewById(R.id.tvEmptyCart)
        tvSubtotal = findViewById(R.id.tvCartSubtotal)
        tvSavings = findViewById(R.id.tvCartSavings)
        tvTotal = findViewById(R.id.tvCartTotal)
        btnCheckout = findViewById(R.id.btnCheckout)
        btnClearCart = findViewById(R.id.btnClearCart)
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            onIncreaseClick = { listingId ->
                handleIncrease(listingId)
            },
            onDecreaseClick = { listingId ->
                handleDecrease(listingId)
            },
            onRemoveItem = { item ->
                handleRemoveItem(item)
            }
        )

        rvCartItems.apply {
            layoutManager = LinearLayoutManager(this@CartActivity)
            adapter = cartAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnCheckout.setOnClickListener {
            handleCheckout()
        }

        btnClearCart.setOnClickListener {
            showClearCartDialog()
        }
    }

    private fun observeCart() {
        CartManager.cartItems.observe(this) { items ->
            // Create a new immutable list to ensure DiffUtil properly detects changes
            cartAdapter.submitList(items.toList())

            if (items.isEmpty()) {
                tvEmptyCart.visibility = View.VISIBLE
                rvCartItems.visibility = View.GONE
                btnCheckout.isEnabled = false
                btnClearCart.visibility = View.GONE
            } else {
                tvEmptyCart.visibility = View.GONE
                rvCartItems.visibility = View.VISIBLE
                btnCheckout.isEnabled = true
                btnClearCart.visibility = View.VISIBLE
            }
        }

        CartManager.totalPrice.observe(this) { total ->
            tvSubtotal.text = "$%.2f".format(total)
            tvTotal.text = "$%.2f".format(total)
        }

        CartManager.totalSavings.observe(this) { savings ->
            tvSavings.text = "-$%.2f".format(savings)
        }
    }

    private fun handleIncrease(listingId: Long) {
        // Get fresh data from CartManager
        val currentQuantity = CartManager.getItemQuantity(listingId)
        val currentItems = CartManager.cartItems.value ?: return
        val item = currentItems.find { it.listingId == listingId } ?: return

        if (currentQuantity < item.maxQuantity) {
            CartManager.increaseQuantity(listingId)
        } else {
            Toast.makeText(this, "Maximum quantity is ${item.maxQuantity}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleDecrease(listingId: Long) {
        // Get fresh data from CartManager
        val currentQuantity = CartManager.getItemQuantity(listingId)

        if (currentQuantity > 1) {
            CartManager.decreaseQuantity(listingId)
        } else {
            // Show confirmation dialog when trying to decrease from 1 to 0
            val currentItems = CartManager.cartItems.value ?: return
            val item = currentItems.find { it.listingId == listingId } ?: return
            showRemoveItemDialog(item)
        }
    }

    private fun handleQuantityChange(item: CartItem, newQuantity: Int) {
        if (newQuantity <= 0) {
            showRemoveItemDialog(item)
        } else if (newQuantity > item.maxQuantity) {
            Toast.makeText(this, "Maximum quantity is ${item.maxQuantity}", Toast.LENGTH_SHORT).show()
        } else {
            CartManager.updateQuantity(item.listingId, newQuantity)
        }
    }

    private fun handleRemoveItem(item: CartItem) {
        showRemoveItemDialog(item)
    }

    private fun showRemoveItemDialog(item: CartItem) {
        AlertDialog.Builder(this)
            .setTitle("Remove Item")
            .setMessage("Remove ${item.title} from cart?")
            .setPositiveButton("Remove") { _, _ ->
                CartManager.removeItem(item.listingId)
                Toast.makeText(this, "Item removed", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showClearCartDialog() {
        AlertDialog.Builder(this)
            .setTitle("Clear Cart")
            .setMessage("Remove all items from cart?")
            .setPositiveButton("Clear") { _, _ ->
                CartManager.clearCart()
                Toast.makeText(this, "Cart cleared", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun handleCheckout() {
        val itemCount = CartManager.getItemCount()
        val total = CartManager.totalPrice.value ?: 0.0

        Toast.makeText(
            this,
            "Proceeding to checkout\n$itemCount items - $${"%.2f".format(total)}",
            Toast.LENGTH_LONG
        ).show()
    }
}
