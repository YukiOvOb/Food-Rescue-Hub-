package com.example.foodrescuehub.ui.orders

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.foodrescuehub.R
import com.example.foodrescuehub.data.model.OrderListingInfo
import com.example.foodrescuehub.data.api.RetrofitClient
import com.example.foodrescuehub.databinding.ActivityOrderDetailBinding
import com.example.foodrescuehub.ui.home.HomeActivity
import com.example.foodrescuehub.ui.review.ReviewActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Order Detail Activity - Display detailed information about an order.
 * Updated to support PENDING_PAYMENT and PAID statuses.
 */
class OrderDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        const val EXTRA_ORDER_ID = "order_id"
        const val EXTRA_CONSUMER_ID = "consumer_id"
    }

    private lateinit var binding: ActivityOrderDetailBinding
    
    private var orderId: Long = 0
    private var consumerId: Long = 0
    private var storeLat: Double? = null
    private var storeLng: Double? = null
    private var consumerLat: Double? = null
    private var consumerLng: Double? = null
    private var googleMap: GoogleMap? = null

    private var orderStatus: String = ""
    private var storeName: String = ""
    private var pickupSlotStart: String? = null
    private var pickupSlotEnd: String? = null
    private var reviewableListings: List<OrderListingInfo> = emptyList()
    private var reviewedListings: List<OrderListingInfo> = emptyList()
    private var allListings: List<OrderListingInfo> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        orderId = intent.getLongExtra(EXTRA_ORDER_ID, 0)
        consumerId = intent.getLongExtra(EXTRA_CONSUMER_ID, 1)

        setupToolbar()
        setupButtons()

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)

        loadOrderDetails()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupButtons() {
        binding.btnGetDirections.setOnClickListener {
            if (storeLat != null && storeLng != null) {
                if (consumerLat != null && consumerLng != null) {
                    openMapsWithRoute(consumerLat!!, consumerLng!!, storeLat!!, storeLng!!)
                } else {
                    openMapsForDirections(storeLat!!, storeLng!!)
                }
            } else {
                Toast.makeText(this, "Store location not available", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnReviewListing.setOnClickListener {
            openReviewFlow()
        }
    }

    private fun loadOrderDetails() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getOrderById(orderId)
                if (response.isSuccessful) {
                    val order = response.body()
                    order?.let { displayOrderDetails(it) }
                } else {
                    Toast.makeText(this@OrderDetailActivity, "Failed to load order details", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@OrderDetailActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayOrderDetails(order: com.example.foodrescuehub.data.model.Order) {
        binding.tvOrderId.text = "Order #${order.orderId}"
        orderStatus = order.status
        storeName = order.store?.storeName ?: "Store"
        pickupSlotStart = order.pickupSlotStart
        pickupSlotEnd = order.pickupSlotEnd

        val pickupWindow = if (order.pickupSlotStart != null && order.pickupSlotEnd != null) {
            "${formatTime(order.pickupSlotStart)} - ${formatTime(order.pickupSlotEnd)}"
        } else {
            "Not specified"
        }
        binding.tvPickupWindow.text = "Pickup Window: $pickupWindow"

        binding.tvStoreName.text = storeName
        binding.tvStoreAddress.text = order.store?.addressLine ?: "Address not available"
        storeLat = order.store?.lat
        storeLng = order.store?.lng
        consumerLat = order.consumer?.defaultLat
        consumerLng = order.consumer?.defaultLng

        updateMapMarkers()
        binding.tvTotalAmount.text = "$${String.format("%.2f", order.totalAmount)}"
        updateOrderStatus(order.status)
        setupQRCodeButton(order.status)
        order.pickupSlotEnd?.let { calculateETA(it) }

        // Get all listings from this order
        allListings = order.orderItems.orEmpty()
            .mapNotNull { it.listing }
            .distinctBy { it.listingId }

        // Separate reviewed and unreviewed listings
        reviewableListings = allListings.filter { !it.hasReviewed }
        reviewedListings = allListings.filter { it.hasReviewed }

        updateReviewButton(order.status)

        binding.llOrderItems.removeAllViews()
        order.orderItems?.forEach { item ->
            val itemView = LayoutInflater.from(this).inflate(R.layout.item_order_item, binding.llOrderItems, false)
            val tvItemName = itemView.findViewById<TextView>(R.id.tvItemName)
            val tvItemQuantity = itemView.findViewById<TextView>(R.id.tvItemQuantity)
            val tvItemPrice = itemView.findViewById<TextView>(R.id.tvItemPrice)

            tvItemName.text = item.listing?.title ?: "Item"
            tvItemQuantity.text = "Qty: ${item.quantity} × $${String.format("%.2f", item.unitPrice)}"
            tvItemPrice.text = "$${String.format("%.2f", item.lineTotal)}"
            binding.llOrderItems.addView(itemView)
        }
    }

    private fun updateReviewButton(status: String) {
        val isCompleted = status.equals("COMPLETED", ignoreCase = true) ||
            status.equals("COLLECTED", ignoreCase = true)

        if (!isCompleted) {
            // Order not completed - hide review button
            binding.btnReviewListing.visibility = View.GONE
            return
        }

        if (reviewableListings.isNotEmpty()) {
            // Has unreviewed listings - show "Write Review" button
            binding.btnReviewListing.text = getString(R.string.review_listing_button)
            binding.btnReviewListing.visibility = View.VISIBLE
        } else if (reviewedListings.isNotEmpty()) {
            // All listings reviewed - show "View Reviews" button
            binding.btnReviewListing.text = "View Reviews"
            binding.btnReviewListing.visibility = View.VISIBLE
        } else {
            // No listings - hide button
            binding.btnReviewListing.visibility = View.GONE
        }
    }

    private fun openReviewFlow() {
        // Check if user wants to view existing reviews or write new ones
        if (reviewableListings.isEmpty() && reviewedListings.isNotEmpty()) {
            // All listings reviewed - show existing reviews
            showExistingReviews()
            return
        }

        if (reviewableListings.isEmpty()) {
            Toast.makeText(this, getString(R.string.review_missing_listing), Toast.LENGTH_SHORT).show()
            return
        }

        if (reviewableListings.size == 1) {
            launchReviewActivity(reviewableListings.first())
            return
        }

        val titles = reviewableListings.map { listing ->
            if (listing.title.isBlank()) getString(R.string.review_listing_fallback) else listing.title
        }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.review_pick_listing))
            .setItems(titles) { _, index ->
                launchReviewActivity(reviewableListings[index])
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showExistingReviews() {
        if (reviewedListings.isEmpty()) {
            Toast.makeText(this, "No reviews found", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a dialog to show existing reviews
        val items = reviewedListings.map { listing ->
            "${listing.title}\n✓ Review submitted"
        }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Reviews for Order #$orderId")
            .setItems(items) { _, index ->
                // Show detailed review info
                val listing = reviewedListings[index]
                Toast.makeText(
                    this,
                    "Review submitted for ${listing.title}\nThank you for your feedback!",
                    Toast.LENGTH_LONG
                ).show()
            }
            .setPositiveButton("OK", null)
            .show()
    }

    private fun launchReviewActivity(listing: OrderListingInfo) {
        val intent = Intent(this, ReviewActivity::class.java).apply {
            putExtra(ReviewActivity.EXTRA_ORDER_ID, orderId)
            putExtra(ReviewActivity.EXTRA_LISTING_ID, listing.listingId)
            putExtra(ReviewActivity.EXTRA_LISTING_TITLE, listing.title)
        }
        startActivity(intent)
    }

    private fun setupQRCodeButton(status: String) {
        when (status.uppercase()) {
            "ACCEPTED", "READY" -> {
                // Store accepted or ready - can view QR code, use green color
                binding.btnViewQrCode.isEnabled = true
                binding.btnViewQrCode.alpha = 1.0f
                binding.btnViewQrCode.setTextColor(android.graphics.Color.WHITE)
                binding.btnViewQrCode.backgroundTintList = android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor("#2d6a5a")
                )
                binding.btnViewQrCode.setOnClickListener { navigateToQRCode() }
            }
            "PENDING_PAYMENT", "PENDING" -> {
                // Waiting for payment - dark gray background, white text
                binding.btnViewQrCode.isEnabled = false
                binding.btnViewQrCode.alpha = 1.0f
                binding.btnViewQrCode.setTextColor(android.graphics.Color.WHITE)
                binding.btnViewQrCode.backgroundTintList = android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor("#666666")
                )
                binding.btnViewQrCode.setOnClickListener(null)
            }
            else -> {
                // Cannot view QR code (PAID, COMPLETED, CANCELLED) - light gray
                binding.btnViewQrCode.isEnabled = false
                binding.btnViewQrCode.alpha = 1.0f
                binding.btnViewQrCode.setTextColor(android.graphics.Color.WHITE)
                binding.btnViewQrCode.backgroundTintList = android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor("#CCCCCC")
                )
                binding.btnViewQrCode.setOnClickListener(null)
            }
        }
    }

    private fun navigateToQRCode() {
        val intent = Intent(this, com.example.foodrescuehub.ui.qrcode.QRCodeActivity::class.java).apply {
            putExtra(com.example.foodrescuehub.ui.qrcode.QRCodeActivity.EXTRA_ORDER_ID, orderId)
            putExtra(com.example.foodrescuehub.ui.qrcode.QRCodeActivity.EXTRA_STORE_NAME, storeName)
            putExtra(com.example.foodrescuehub.ui.qrcode.QRCodeActivity.EXTRA_PICKUP_START, pickupSlotStart)
            putExtra(com.example.foodrescuehub.ui.qrcode.QRCodeActivity.EXTRA_PICKUP_END, pickupSlotEnd)
        }
        startActivity(intent)
    }

    private fun updateOrderStatus(status: String) {
        when (status.uppercase()) {
            "PENDING_PAYMENT", "PENDING" -> {
                // Waiting for payment - all status icons gray
                setStatusIcon(binding.statusPaidIcon, false)
                setStatusIcon(binding.statusReadyIcon, false)
                setStatusIcon(binding.statusCollectedIcon, false)
                setProgressLine(binding.progressLine1, false)
                setProgressLine(binding.progressLine2, false)
            }
            "PAID" -> {
                // Paid, waiting for store confirmation - all icons orange
                setStatusIcon(binding.statusPaidIcon, false, false, true)
                setStatusIcon(binding.statusReadyIcon, false, false, true)
                setStatusIcon(binding.statusCollectedIcon, false, false, true)
                setProgressLine(binding.progressLine1, false, false, true)
                setProgressLine(binding.progressLine2, false, false, true)
            }
            "ACCEPTED" -> {
                // Store accepted - only first icon green, second icon orange
                setStatusIcon(binding.statusPaidIcon, true)
                setStatusIcon(binding.statusReadyIcon, false, false, true)
                setStatusIcon(binding.statusCollectedIcon, false)
                setProgressLine(binding.progressLine1, true)
                setProgressLine(binding.progressLine2, false)
            }
            "READY" -> {
                // Ready for pickup - first two icons green
                setStatusIcon(binding.statusPaidIcon, true)
                setStatusIcon(binding.statusReadyIcon, true)
                setStatusIcon(binding.statusCollectedIcon, false)
                setProgressLine(binding.progressLine1, true)
                setProgressLine(binding.progressLine2, false)
            }
            "COMPLETED" -> {
                // Order completed (QR code scanned)
                setStatusIcon(binding.statusPaidIcon, true)
                setStatusIcon(binding.statusReadyIcon, true)
                setStatusIcon(binding.statusCollectedIcon, true)
                setProgressLine(binding.progressLine1, true)
                setProgressLine(binding.progressLine2, true)
            }
            "CANCELLED" -> {
                // Order cancelled
                setStatusIcon(binding.statusPaidIcon, false, true)
                setStatusIcon(binding.statusReadyIcon, false, true)
                setStatusIcon(binding.statusCollectedIcon, false, true)
                setProgressLine(binding.progressLine1, false, true)
                setProgressLine(binding.progressLine2, false, true)
            }
        }
    }

    private fun setStatusIcon(icon: TextView, completed: Boolean, cancelled: Boolean = false, pending: Boolean = false) {
        when {
            cancelled -> {
                icon.text = "✕"
                icon.setBackgroundResource(R.drawable.bg_status_cancelled)
                icon.setTextColor(getColor(android.R.color.white))
            }
            pending -> {
                icon.text = "○"
                icon.setBackgroundResource(R.drawable.bg_status_pending)
                icon.setTextColor(getColor(android.R.color.white))
            }
            completed -> {
                icon.text = "✓"
                icon.setBackgroundResource(R.drawable.bg_status_confirmed)
                icon.setTextColor(getColor(android.R.color.white))
            }
            else -> {
                icon.text = "○"
                icon.setBackgroundResource(R.drawable.bg_status_pending)
                icon.setTextColor(getColor(android.R.color.darker_gray))
            }
        }
    }

    private fun setProgressLine(line: View, completed: Boolean, cancelled: Boolean = false, pending: Boolean = false) {
        line.setBackgroundColor(when {
            cancelled -> getColor(R.color.status_cancelled)
            pending -> android.graphics.Color.parseColor("#FFA000")
            completed -> getColor(R.color.status_confirmed)
            else -> getColor(android.R.color.darker_gray)
        })
    }

    private fun calculateETA(pickupEndTime: String) {
        try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val endDate = format.parse(pickupEndTime)
            val now = Date()
            if (endDate != null && endDate.after(now)) {
                val diffInMillis = endDate.time - now.time
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis)
                if (minutes > 0) {
                    binding.tvEtaWarning.text = "ETA: Time left to pick up: $minutes mins"
                    binding.llEtaWarning.visibility = View.VISIBLE
                }
            }
        } catch (e: Exception) {
            binding.llEtaWarning.visibility = View.GONE
        }
    }

    private fun formatTime(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            date?.let { outputFormat.format(it) } ?: dateString
        } catch (e: Exception) {
            dateString
        }
    }

    private fun openMapsForDirections(lat: Double, lng: Double) {
        val uri = Uri.parse("geo:0,0?q=$lat,$lng")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q=$lat,$lng"))
            startActivity(browserIntent)
        }
    }

    private fun openMapsWithRoute(originLat: Double, originLng: Double, destLat: Double, destLng: Double) {
        val uri = Uri.parse("https://www.google.com/maps/dir/?api=1&origin=$originLat,$originLng&destination=$destLat,$destLng&travelmode=driving")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            val browserIntent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(browserIntent)
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isZoomControlsEnabled = true
        updateMapMarkers()
    }

    private fun updateMapMarkers() {
        val map = googleMap ?: return
        map.clear()
        val builder = LatLngBounds.Builder()
        var hasPoints = false
        if (storeLat != null && storeLng != null) {
            val storePos = LatLng(storeLat!!, storeLng!!)
            map.addMarker(MarkerOptions().position(storePos).title(storeName).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))
            builder.include(storePos)
            hasPoints = true
        }
        if (consumerLat != null && consumerLng != null) {
            val consumerPos = LatLng(consumerLat!!, consumerLng!!)
            map.addMarker(MarkerOptions().position(consumerPos).title("Your Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
            builder.include(consumerPos)
            hasPoints = true
        }
        if (hasPoints) {
            try {
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100))
            } catch (e: Exception) {
                storeLat?.let { lat -> storeLng?.let { lng -> map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), 14f)) } }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
        // Reload order details to refresh review button state
        loadOrderDetails()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }
}
