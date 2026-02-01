package com.example.foodrescuehub.ui.orders

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.foodrescuehub.R
import com.example.foodrescuehub.data.api.RetrofitClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Order Detail Activity - Display detailed information about an order
 */
class OrderDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        const val EXTRA_ORDER_ID = "order_id"
        const val EXTRA_CONSUMER_ID = "consumer_id"
    }

    private lateinit var toolbar: MaterialToolbar
    private lateinit var mapView: MapView
    private lateinit var tvOrderId: TextView
    private lateinit var tvPickupWindow: TextView
    private lateinit var tvStoreName: TextView
    private lateinit var tvStoreAddress: TextView
    private lateinit var tvTotalAmount: TextView
    private lateinit var tvEtaWarning: TextView
    private lateinit var llEtaWarning: LinearLayout
    private lateinit var llOrderItems: LinearLayout
    private lateinit var btnViewQrCode: MaterialButton
    private lateinit var btnGetDirections: MaterialButton

    private lateinit var statusPaidIcon: TextView
    private lateinit var statusReadyIcon: TextView
    private lateinit var statusCollectedIcon: TextView
    private lateinit var progressLine1: View
    private lateinit var progressLine2: View

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail)

        // Get order ID from intent
        orderId = intent.getLongExtra(EXTRA_ORDER_ID, 0)
        consumerId = intent.getLongExtra(EXTRA_CONSUMER_ID, 1)

        initViews()
        setupToolbar()
        setupButtons()

        // Initialize MapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        loadOrderDetails()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        mapView = findViewById(R.id.mapView)
        tvOrderId = findViewById(R.id.tvOrderId)
        tvPickupWindow = findViewById(R.id.tvPickupWindow)
        tvStoreName = findViewById(R.id.tvStoreName)
        tvStoreAddress = findViewById(R.id.tvStoreAddress)
        tvTotalAmount = findViewById(R.id.tvTotalAmount)
        tvEtaWarning = findViewById(R.id.tvEtaWarning)
        llEtaWarning = findViewById(R.id.llEtaWarning)
        llOrderItems = findViewById(R.id.llOrderItems)
        btnViewQrCode = findViewById(R.id.btnViewQrCode)
        btnGetDirections = findViewById(R.id.btnGetDirections)

        statusPaidIcon = findViewById(R.id.statusPaidIcon)
        statusReadyIcon = findViewById(R.id.statusReadyIcon)
        statusCollectedIcon = findViewById(R.id.statusCollectedIcon)
        progressLine1 = findViewById(R.id.progressLine1)
        progressLine2 = findViewById(R.id.progressLine2)
    }

    private fun setupToolbar() {
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupButtons() {
        // QR Code button logic will be set based on order status in displayOrderDetails()

        btnGetDirections.setOnClickListener {
            if (storeLat != null && storeLng != null) {
                // If we have consumer location, show route from consumer to store
                if (consumerLat != null && consumerLng != null) {
                    openMapsWithRoute(consumerLat!!, consumerLng!!, storeLat!!, storeLng!!)
                } else {
                    // Otherwise just show the store location
                    openMapsForDirections(storeLat!!, storeLng!!)
                }
            } else {
                Toast.makeText(this, "Store location not available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadOrderDetails() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getOrderById(consumerId, orderId)

                if (response.isSuccessful) {
                    val order = response.body()
                    order?.let { displayOrderDetails(it) }
                } else {
                    Toast.makeText(
                        this@OrderDetailActivity,
                        "Failed to load order details",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@OrderDetailActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun displayOrderDetails(order: com.example.foodrescuehub.data.model.Order) {
        // Order ID
        tvOrderId.text = "Order #${order.orderId}"

        // Save order info for QR code
        orderStatus = order.status
        storeName = order.store?.storeName ?: "Store"
        pickupSlotStart = order.pickupSlotStart
        pickupSlotEnd = order.pickupSlotEnd

        // Pickup Window
        val pickupWindow = if (order.pickupSlotStart != null && order.pickupSlotEnd != null) {
            "${formatTime(order.pickupSlotStart)} - ${formatTime(order.pickupSlotEnd)}"
        } else {
            "Not specified"
        }
        tvPickupWindow.text = "Pickup Window: $pickupWindow"

        // Store Info
        tvStoreName.text = storeName
        tvStoreAddress.text = order.store?.addressLine ?: "Address not available"
        storeLat = order.store?.lat
        storeLng = order.store?.lng

        // Consumer Location
        consumerLat = order.consumer?.defaultLat
        consumerLng = order.consumer?.defaultLng

        // Debug logs
        android.util.Log.d("OrderDetail", "Store: ${order.store?.storeName}")
        android.util.Log.d("OrderDetail", "Store Lat: ${order.store?.lat}, Type: ${order.store?.lat?.javaClass}")
        android.util.Log.d("OrderDetail", "Store Lng: ${order.store?.lng}, Type: ${order.store?.lng?.javaClass}")
        android.util.Log.d("OrderDetail", "Final storeLat: $storeLat, storeLng: $storeLng")
        android.util.Log.d("OrderDetail", "Consumer Lat: $consumerLat, Lng: $consumerLng")

        // Update map if ready
        updateMapMarkers()

        // Total Amount
        tvTotalAmount.text = "$${String.format("%.2f", order.totalAmount)}"

        // Update Status Progress
        updateOrderStatus(order.status)

        // Setup QR Code button based on order status
        setupQRCodeButton(order.status)

        // Calculate ETA
        order.pickupSlotEnd?.let { calculateETA(it) }

        // Order Items
        llOrderItems.removeAllViews()
        order.orderItems?.forEach { item ->
            val itemView = LayoutInflater.from(this)
                .inflate(R.layout.item_order_item, llOrderItems, false)

            val tvItemName = itemView.findViewById<TextView>(R.id.tvItemName)
            val tvItemQuantity = itemView.findViewById<TextView>(R.id.tvItemQuantity)
            val tvItemPrice = itemView.findViewById<TextView>(R.id.tvItemPrice)

            tvItemName.text = item.listing?.title ?: "Item"
            tvItemQuantity.text = "Qty: ${item.quantity} × $${String.format("%.2f", item.unitPrice)}"
            tvItemPrice.text = "$${String.format("%.2f", item.lineTotal)}"

            llOrderItems.addView(itemView)
        }
    }

    private fun setupQRCodeButton(status: String) {
        when (status.uppercase()) {
            "CANCELLED" -> {
                // Grey, disabled
                btnViewQrCode.isEnabled = false
                btnViewQrCode.backgroundTintList = android.content.res.ColorStateList.valueOf(
                    getColor(android.R.color.darker_gray)
                )
                btnViewQrCode.setOnClickListener(null)
            }
            "READY", "COMPLETED", "COLLECTED" -> {
                // Green, enabled, navigate to QR code
                btnViewQrCode.isEnabled = true
                btnViewQrCode.backgroundTintList = android.content.res.ColorStateList.valueOf(
                    getColor(R.color.status_confirmed)
                )
                btnViewQrCode.setOnClickListener {
                    navigateToQRCode()
                }
            }
            else -> {
                // Yellow, enabled but show toast (PENDING, ACCEPTED, CONFIRMED)
                btnViewQrCode.isEnabled = true
                btnViewQrCode.backgroundTintList = android.content.res.ColorStateList.valueOf(
                    getColor(R.color.status_pending)
                )
                btnViewQrCode.setOnClickListener {
                    Toast.makeText(
                        this,
                        "Order must be ready before viewing QR code. Please wait.",
                        Toast.LENGTH_LONG
                    ).show()
                }
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
            "PENDING" -> {
                // Only first step - Pending
                setStatusIcon(statusPaidIcon, true)
                setStatusIcon(statusReadyIcon, false)
                setStatusIcon(statusCollectedIcon, false)
                setProgressLine(progressLine1, false)
                setProgressLine(progressLine2, false)
            }
            "ACCEPTED", "CONFIRMED" -> {
                // First step completed - Accepted
                setStatusIcon(statusPaidIcon, true)
                setStatusIcon(statusReadyIcon, false)
                setStatusIcon(statusCollectedIcon, false)
                setProgressLine(progressLine1, false)
                setProgressLine(progressLine2, false)
            }
            "READY" -> {
                // First two steps - Accepted + Ready
                setStatusIcon(statusPaidIcon, true)
                setStatusIcon(statusReadyIcon, true)
                setStatusIcon(statusCollectedIcon, false)
                setProgressLine(progressLine1, true)
                setProgressLine(progressLine2, false)
            }
            "COMPLETED", "COLLECTED" -> {
                // All steps completed
                setStatusIcon(statusPaidIcon, true)
                setStatusIcon(statusReadyIcon, true)
                setStatusIcon(statusCollectedIcon, true)
                setProgressLine(progressLine1, true)
                setProgressLine(progressLine2, true)
            }
            "CANCELLED" -> {
                // Show all as cancelled/inactive
                setStatusIcon(statusPaidIcon, false, true)
                setStatusIcon(statusReadyIcon, false, true)
                setStatusIcon(statusCollectedIcon, false, true)
                setProgressLine(progressLine1, false, true)
                setProgressLine(progressLine2, false, true)
            }
        }
    }

    private fun setStatusIcon(icon: TextView, completed: Boolean, cancelled: Boolean = false) {
        when {
            cancelled -> {
                icon.text = "✕"
                icon.setBackgroundResource(R.drawable.bg_status_cancelled)
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

    private fun setProgressLine(line: View, completed: Boolean, cancelled: Boolean = false) {
        line.setBackgroundColor(
            when {
                cancelled -> getColor(R.color.status_cancelled)
                completed -> getColor(R.color.status_confirmed)
                else -> getColor(android.R.color.darker_gray)
            }
        )
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
                    tvEtaWarning.text = "ETA: Time left to pick up: $minutes mins"
                    llEtaWarning.visibility = View.VISIBLE
                }
            }
        } catch (e: Exception) {
            llEtaWarning.visibility = View.GONE
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
            // Fallback to browser
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q=$lat,$lng"))
            startActivity(browserIntent)
        }
    }

    /**
     * Open Google Maps with route from origin to destination
     */
    private fun openMapsWithRoute(originLat: Double, originLng: Double, destLat: Double, destLng: Double) {
        // Use Google Maps Directions API URL format
        val uri = Uri.parse("https://www.google.com/maps/dir/?api=1&origin=$originLat,$originLng&destination=$destLat,$destLng&travelmode=driving")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            // Fallback to browser if Google Maps is not installed
            val browserIntent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(browserIntent)
        }
    }

    // ========== Google Maps Integration ==========

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.apply {
            isZoomControlsEnabled = true
            isMyLocationButtonEnabled = false
            isMapToolbarEnabled = false
        }

        updateMapMarkers()
    }

    private fun updateMapMarkers() {
        val map = googleMap ?: return

        // Check if we have location data
        if (storeLat == null || storeLng == null) {
            return
        }

        map.clear()

        val bounds = LatLngBounds.Builder()
        var hasMarkers = false

        // Add store marker
        val storeLocation = LatLng(storeLat!!, storeLng!!)
        map.addMarker(
            MarkerOptions()
                .position(storeLocation)
                .title("Store Location")
                .snippet(tvStoreAddress.text.toString())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )
        bounds.include(storeLocation)
        hasMarkers = true

        // Add consumer marker if available
        if (consumerLat != null && consumerLng != null) {
            val consumerLocation = LatLng(consumerLat!!, consumerLng!!)
            map.addMarker(
                MarkerOptions()
                    .position(consumerLocation)
                    .title("Your Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )
            bounds.include(consumerLocation)
        }

        // Adjust camera to show all markers
        if (hasMarkers) {
            try {
                val padding = 150 // padding in pixels
                val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds.build(), padding)
                map.moveCamera(cameraUpdate)
            } catch (e: Exception) {
                // If only one marker or bounds issue, zoom to store location
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(storeLocation, 14f))
            }
        }
    }

    // ========== MapView Lifecycle Methods ==========

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}
