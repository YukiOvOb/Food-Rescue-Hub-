package com.example.foodrescuehub.ui.detail

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.foodrescuehub.R
import com.example.foodrescuehub.data.repository.AuthManager
import com.example.foodrescuehub.data.repository.CartManager
import com.example.foodrescuehub.databinding.ActivityProductDetailBinding
import com.example.foodrescuehub.ui.auth.LoginActivity
import com.example.foodrescuehub.ui.cart.CartActivity
import com.example.foodrescuehub.ui.dialog.LoginPromptDialog
import com.example.foodrescuehub.ui.home.HomeActivity
import com.example.foodrescuehub.util.UrlUtils
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Product Detail Activity
 * Displays detailed information about a listing and handles adding to cart
 * Updated to use ViewBinding
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

    private lateinit var binding: ActivityProductDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadListingData()
        setupClickListeners()
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

        binding.tvProductTitle.text = title
        binding.tvStoreName.text = storeName
        binding.tvCategory.text = category
        binding.tvDistance.text = distance
        binding.tvPrice.text = "$%.2f".format(price)
        binding.tvSavingsLabel.text = savingsLabel

        val pickupWindowText = formatPickupWindow(pickupStart, pickupEnd)
        binding.tvPickupWindow.text = pickupWindowText

        updateButtonState(pickupEnd, qtyAvailable)

        val contentsText = if (description.isNotBlank()) description else "3-5 items ($category items)"
        binding.tvContents.text = contentsText

        val storageText = when (category.lowercase()) {
            "bakery", "cafe", "coffee shop" -> "Room temp"
            "restaurant" -> "Keep refrigerated"
            "supermarket" -> "Mixed (see package)"
            else -> "Room temp"
        }
        binding.tvStorage.text = storageText

        val allergensText = when (category.lowercase()) {
            "bakery" -> "gluten, dairy (may contain nuts)"
            "cafe", "coffee shop" -> "dairy (may contain gluten, nuts)"
            "restaurant" -> "varies by item"
            else -> "See individual items"
        }
        binding.tvAllergens.text = allergensText

        binding.tvListingAccuracy.text = "96%"
        binding.tvOnTimePickup.text = "98%"

        val fullPhotoUrl = UrlUtils.getFullUrl(photoUrl)
        Glide.with(this)
            .load(fullPhotoUrl)
            .placeholder(R.drawable.ic_launcher_foreground)
            .error(R.drawable.ic_launcher_foreground)
            .centerCrop()
            .into(binding.ivProductImage)
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
                    binding.tvPickupWindow.setTextColor(getColor(android.R.color.holo_red_dark))
                }
            } catch (e: Exception) {
            }
        }

        binding.btnBuyNow.isEnabled = isEnabled
        binding.btnBuyNow.text = buttonText
        if (!isEnabled) {
            binding.btnBuyNow.alpha = 0.6f
            binding.btnBuyNow.setBackgroundColor(getColor(android.R.color.darker_gray))
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
        binding.btnBack.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        binding.btnShare.setOnClickListener {
            shareToWhatsApp()
        }

        binding.btnBuyNow.setOnClickListener { addToCart() }
    }

    private fun shareToWhatsApp() {
        // Get listing details
        val title = intent.getStringExtra(EXTRA_LISTING_TITLE) ?: "Mystery Box"
        val storeName = intent.getStringExtra(EXTRA_LISTING_STORE_NAME) ?: ""
        val price = intent.getDoubleExtra(EXTRA_LISTING_PRICE, 0.0)
        val savingsLabel = intent.getStringExtra(EXTRA_LISTING_SAVINGS_LABEL) ?: ""

        // Create share message
        val shareText = buildString {
            append("🍱 Check out this amazing deal!\n\n")
            append("📦 $title\n")
            append("$storeName\n")
            append("💰 Price: $%.2f\n".format(price))
            if (savingsLabel.isNotBlank()) {
                append("💚 $savingsLabel\n")
            }
            append("\nGet it before it's gone! 🚀\n")
            append("\nDownload Food Rescue Hub app now!")
        }

        try {
            // Try to share specifically to WhatsApp
            val whatsappIntent = Intent(Intent.ACTION_SEND)
            whatsappIntent.type = "text/plain"
            whatsappIntent.setPackage("com.whatsapp")
            whatsappIntent.putExtra(Intent.EXTRA_TEXT, shareText)
            startActivity(whatsappIntent)
        } catch (e: Exception) {
            // If WhatsApp is not installed, show general share dialog
            try {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareText)
                startActivity(Intent.createChooser(shareIntent, "Share via"))
            } catch (ex: Exception) {
                Toast.makeText(this, "Unable to share", Toast.LENGTH_SHORT).show()
            }
        }
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
        val dialogView = layoutInflater.inflate(R.layout.dialog_clear_cart, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val btnClearAdd = dialogView.findViewById<Button>(R.id.btnClearAdd)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

        btnClearAdd.setOnClickListener {
            dialog.dismiss()
            CartManager.clearCart { success ->
                if (success) {
                    performAddToCart(listingId)
                }
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
    private fun showSimpleRoundedDialog(title: String, message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()

        // Set rounded corners
        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_rounded_bg)
        }

        dialog.show()
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
