package com.example.foodrescuehub.ui.detail

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.foodrescuehub.R
import com.example.foodrescuehub.data.repository.AuthManager
import com.example.foodrescuehub.data.repository.CartManager
import com.example.foodrescuehub.ui.auth.LoginActivity
import com.example.foodrescuehub.ui.cart.CartActivity
import com.example.foodrescuehub.ui.dialog.LoginPromptDialog
import com.example.foodrescuehub.util.UrlUtils
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Product Detail Activity
 * Displays detailed information about a listing and handles adding to cart
 */
class ProductDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_LISTING_ID = "extra_listing_id"
        const val EXTRA_LISTING_TITLE = "extra_listing_title"
        const val EXTRA_LISTING_STORE_NAME = "extra_listing_store_name"
        const val EXTRA_LISTING_STORE_ID = "extra_listing_store_id"
        const val EXTRA_LISTING_CATEGORY = "extra_listing_category"
        const val EXTRA_LISTING_DISTANCE = "extra_listing_distance"
        const val EXTRA_LISTING_PRICE = "extra_listing_price"
        const val EXTRA_LISTING_ORIGINAL_PRICE = "extra_listing_original_price"
        const val EXTRA_LISTING_SAVINGS_LABEL = "extra_listing_savings_label"
        const val EXTRA_LISTING_PICKUP_START = "extra_listing_pickup_start"
        const val EXTRA_LISTING_PICKUP_END = "extra_listing_pickup_end"
        const val EXTRA_LISTING_DESCRIPTION = "extra_listing_description"
        const val EXTRA_LISTING_QTY_AVAILABLE = "extra_listing_qty_available"
        const val EXTRA_LISTING_PHOTO_URL = "extra_listing_photo_url"
    }

    private lateinit var btnBack: ImageButton
    private lateinit var btnShare: ImageButton
    private lateinit var ivProductImage: ImageView
    private lateinit var tvProductTitle: TextView
    private lateinit var tvStoreName: TextView
    private lateinit var tvCategory: TextView
    private lateinit var tvDistance: TextView
    private lateinit var tvPrice: TextView
    private lateinit var tvSavingsLabel: TextView
    private lateinit var tvPickupWindow: TextView
    private lateinit var tvContents: TextView
    private lateinit var tvStorage: TextView
    private lateinit var tvAllergens: TextView
    private lateinit var tvListingAccuracy: TextView
    private lateinit var tvOnTimePickup: TextView
    private lateinit var btnBuyNow: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        initViews()
        loadListingData()
        setupClickListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        btnShare = findViewById(R.id.btnShare)
        ivProductImage = findViewById(R.id.ivProductImage)
        tvProductTitle = findViewById(R.id.tvProductTitle)
        tvStoreName = findViewById(R.id.tvStoreName)
        tvCategory = findViewById(R.id.tvCategory)
        tvDistance = findViewById(R.id.tvDistance)
        tvPrice = findViewById(R.id.tvPrice)
        tvSavingsLabel = findViewById(R.id.tvSavingsLabel)
        tvPickupWindow = findViewById(R.id.tvPickupWindow)
        tvContents = findViewById(R.id.tvContents)
        tvStorage = findViewById(R.id.tvStorage)
        tvAllergens = findViewById(R.id.tvAllergens)
        tvListingAccuracy = findViewById(R.id.tvListingAccuracy)
        tvOnTimePickup = findViewById(R.id.tvOnTimePickup)
        btnBuyNow = findViewById(R.id.btnBuyNow)
    }

    private fun loadListingData() {
        val title = intent.getStringExtra(EXTRA_LISTING_TITLE) ?: "Mystery Box"
        val storeName = intent.getStringExtra(EXTRA_LISTING_STORE_NAME) ?: ""
        val category = intent.getStringExtra(EXTRA_LISTING_CATEGORY) ?: ""
        val distance = intent.getStringExtra(EXTRA_LISTING_DISTANCE) ?: "2.1 km"
        val price = intent.getDoubleExtra(EXTRA_LISTING_PRICE, 0.0)
        val savingsLabel = intent.getStringExtra(EXTRA_LISTING_SAVINGS_LABEL) ?: ""
        val pickupStart = intent.getStringExtra(EXTRA_LISTING_PICKUP_START) ?: ""
        val pickupEnd = intent.getStringExtra(EXTRA_LISTING_PICKUP_END) ?: ""
        val description = intent.getStringExtra(EXTRA_LISTING_DESCRIPTION) ?: ""
        val qtyAvailable = intent.getIntExtra(EXTRA_LISTING_QTY_AVAILABLE, 0)
        val photoUrl = intent.getStringExtra(EXTRA_LISTING_PHOTO_URL)

        tvProductTitle.text = title
        tvStoreName.text = storeName
        tvCategory.text = category
        tvDistance.text = distance
        tvPrice.text = "$%.2f".format(price)
        tvSavingsLabel.text = savingsLabel

        val pickupWindowText = formatPickupWindow(pickupStart, pickupEnd)
        tvPickupWindow.text = pickupWindowText

        updateButtonState(pickupEnd, qtyAvailable)

        val contentsText = if (description.isNotBlank()) description else "3-5 items ($category items)"
        tvContents.text = contentsText

        val storageText = when (category.lowercase()) {
            "bakery", "cafe", "coffee shop" -> "Room temp"
            "restaurant" -> "Keep refrigerated"
            "supermarket" -> "Mixed (see package)"
            else -> "Room temp"
        }
        tvStorage.text = storageText

        val allergensText = when (category.lowercase()) {
            "bakery" -> "gluten, dairy (may contain nuts)"
            "cafe", "coffee shop" -> "dairy (may contain gluten, nuts)"
            "restaurant" -> "varies by item"
            else -> "See individual items"
        }
        tvAllergens.text = allergensText

        tvListingAccuracy.text = "96%"
        tvOnTimePickup.text = "98%"

        // USE UrlUtils for image loading
        val fullPhotoUrl = UrlUtils.getFullUrl(photoUrl)
        Glide.with(this)
            .load(fullPhotoUrl)
            .placeholder(R.drawable.ic_launcher_foreground)
            .error(R.drawable.ic_launcher_foreground)
            .centerCrop()
            .into(ivProductImage)
    }

    private fun updateButtonState(pickupEndTime: String, qtyAvailable: Int) {
        var isEnabled = true
        var buttonText = "Buy Now"

        if (qtyAvailable <= 0) {
            isEnabled = false
            buttonText = "Sold Out"
        }

        if (isEnabled && pickupEndTime.isNotBlank()) {
            try {
                val end = LocalDateTime.parse(pickupEndTime, DateTimeFormatter.ISO_DATE_TIME)
                if (LocalDateTime.now().isAfter(end)) {
                    isEnabled = false
                    buttonText = "Expired"
                    tvPickupWindow.setTextColor(getColor(android.R.color.holo_red_dark))
                }
            } catch (e: Exception) {
            }
        }

        btnBuyNow.isEnabled = isEnabled
        btnBuyNow.text = buttonText
        if (!isEnabled) {
            btnBuyNow.alpha = 0.6f
            btnBuyNow.setBackgroundColor(getColor(android.R.color.darker_gray))
        }
    }

    private fun formatPickupWindow(startTime: String, endTime: String): String {
        return try {
            val formatter = DateTimeFormatter.ISO_DATE_TIME
            val start = LocalDateTime.parse(startTime, formatter)
            val end = LocalDateTime.parse(endTime, formatter)
            val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
            "${start.format(timeFormatter)} - ${end.format(timeFormatter)}"
        } catch (e: Exception) {
            "Pickup time available"
        }
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener { finish() }
        btnShare.setOnClickListener {
            Toast.makeText(this, "Share feature coming soon", Toast.LENGTH_SHORT).show()
        }
        btnBuyNow.setOnClickListener { addToCart() }
    }

    private fun addToCart() {
        if (!AuthManager.isUserLoggedIn()) {
            LoginPromptDialog.show(this) {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
            return
        }

        val listingId = intent.getLongExtra(EXTRA_LISTING_ID, 0L)
        val storeId = intent.getLongExtra(EXTRA_LISTING_STORE_ID, 0L)
        
        if (listingId == 0L) {
            Toast.makeText(this, "Error: Invalid Product", Toast.LENGTH_SHORT).show()
            return
        }

        val currentSupplierId = CartManager.supplierId.value
        if (currentSupplierId != null && currentSupplierId != 0L && currentSupplierId != storeId) {
            showCrossStoreWarning(listingId)
        } else {
            performAddToCart(listingId)
        }
    }

    private fun showCrossStoreWarning(listingId: Long) {
        AlertDialog.Builder(this)
            .setTitle("Clear Cart?")
            .setMessage("Your cart contains items from another store. Adding this item will clear your current cart. Continue?")
            .setPositiveButton("Clear & Add") { _, _ ->
                CartManager.clearCart { success ->
                    if (success) {
                        performAddToCart(listingId)
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performAddToCart(listingId: Long) {
        CartManager.addItem(listingId, 1) { success, error ->
            if (success) {
                Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, CartActivity::class.java))
            } else {
                Toast.makeText(this, error ?: "Failed to add to cart", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
