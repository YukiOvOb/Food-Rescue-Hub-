package com.example.foodrescuehub.ui.detail

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.foodrescuehub.R
import com.example.foodrescuehub.data.model.CartItem
import com.example.foodrescuehub.data.repository.CartManager
import com.example.foodrescuehub.ui.cart.CartActivity
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Product Detail Activity
 * Displays detailed information about a listing including:
 * - Product image and title
 * - Store information
 * - Pricing and savings
 * - Pickup window and contents
 * - Allergens and storage info
 * - Reliability metrics
 */
class ProductDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_LISTING_ID = "extra_listing_id"
        const val EXTRA_LISTING_TITLE = "extra_listing_title"
        const val EXTRA_LISTING_STORE_NAME = "extra_listing_store_name"
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
        // Get data from intent
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

        // Set data to views
        tvProductTitle.text = title
        tvStoreName.text = storeName
        tvCategory.text = category
        tvDistance.text = distance
        tvPrice.text = "$%.2f".format(price)
        tvSavingsLabel.text = savingsLabel

        // Format pickup window
        val pickupWindowText = formatPickupWindow(pickupStart, pickupEnd)
        tvPickupWindow.text = pickupWindowText

        // Contents (use description or default)
        val contentsText = if (description.isNotBlank()) {
            description
        } else {
            "3-5 items ($category items)"
        }
        tvContents.text = contentsText

        // Storage info (based on category)
        val storageText = when (category.lowercase()) {
            "bakery", "cafe", "coffee shop" -> "Room temp"
            "restaurant" -> "Keep refrigerated"
            "supermarket" -> "Mixed (see package)"
            else -> "Room temp"
        }
        tvStorage.text = storageText

        // Allergens (placeholder - should come from backend)
        val allergensText = when (category.lowercase()) {
            "bakery" -> "gluten, dairy (may contain nuts)"
            "cafe", "coffee shop" -> "dairy (may contain gluten, nuts)"
            "restaurant" -> "varies by item"
            else -> "See individual items"
        }
        tvAllergens.text = allergensText

        // Reliability metrics (placeholder - should come from backend)
        tvListingAccuracy.text = "96%"
        tvOnTimePickup.text = "98%"

        // Load image
        if (!photoUrl.isNullOrBlank()) {
            Glide.with(this)
                .load(photoUrl)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .centerCrop()
                .into(ivProductImage)
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
        btnBack.setOnClickListener {
            finish()
        }

        btnShare.setOnClickListener {
            Toast.makeText(this, "Share feature coming soon", Toast.LENGTH_SHORT).show()
        }

        btnBuyNow.setOnClickListener {
            addToCart()
        }
    }

    private fun addToCart() {
        val listingId = intent.getLongExtra(EXTRA_LISTING_ID, 0L)
        val title = intent.getStringExtra(EXTRA_LISTING_TITLE) ?: "Mystery Box"
        val storeName = intent.getStringExtra(EXTRA_LISTING_STORE_NAME) ?: ""
        val price = intent.getDoubleExtra(EXTRA_LISTING_PRICE, 0.0)
        val originalPrice = intent.getDoubleExtra(EXTRA_LISTING_ORIGINAL_PRICE, 0.0)
        val photoUrl = intent.getStringExtra(EXTRA_LISTING_PHOTO_URL)
        val qtyAvailable = intent.getIntExtra(EXTRA_LISTING_QTY_AVAILABLE, 10)

        val cartItem = CartItem(
            listingId = listingId,
            title = title,
            storeName = storeName,
            price = price,
            originalPrice = originalPrice,
            photoUrl = photoUrl,
            quantity = 1,
            maxQuantity = qtyAvailable.coerceAtMost(10)
        )

        CartManager.addItem(cartItem)

        Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show()

        // Navigate to cart
        val intent = Intent(this, CartActivity::class.java)
        startActivity(intent)
    }
}
