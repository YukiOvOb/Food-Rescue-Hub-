package com.example.foodrescuehub.ui.location

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.foodrescuehub.R
import com.example.foodrescuehub.data.api.RetrofitClient
import com.example.foodrescuehub.data.model.Listing
import com.example.foodrescuehub.data.model.UpdateLocationRequest
import com.example.foodrescuehub.data.repository.AuthManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import kotlinx.coroutines.launch

/**
 * LocationActivity - Show nearby stores on a map with search/filter functionality
 */
class LocationActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val DEFAULT_ZOOM = 13f
        // Default location: Alice's location in Singapore
        private val DEFAULT_LOCATION = LatLng(1.3521, 103.8198)
    }

    private lateinit var mapView: MapView
    private lateinit var toolbar: MaterialToolbar
    private lateinit var searchView: SearchView
    private lateinit var fabChangeLocation: ExtendedFloatingActionButton
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var googleMap: GoogleMap? = null
    private var currentLocation: LatLng = DEFAULT_LOCATION
    private var allListings: List<Listing> = emptyList()
    private var filteredListings: List<Listing> = emptyList()
    private val storeMarkers = mutableMapOf<Marker, Listing>()
    private var isSelectingLocation = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

        initViews()
        setupToolbar()
        setupSearchView()
        setupChangeLocationButton()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Initialize MapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        // Load listings
        loadNearbyListings()
    }

    private fun initViews() {
        mapView = findViewById(R.id.mapView)
        toolbar = findViewById(R.id.toolbar)
        searchView = findViewById(R.id.searchView)
        fabChangeLocation = findViewById(R.id.fabChangeLocation)
    }

    private fun setupToolbar() {
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterStores(query ?: "")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterStores(newText ?: "")
                return true
            }
        })
    }

    private fun setupChangeLocationButton() {
        fabChangeLocation.setOnClickListener {
            showChangeLocationDialog()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        googleMap?.apply {
            uiSettings.isZoomControlsEnabled = true
            uiSettings.isMyLocationButtonEnabled = true
            uiSettings.isMapToolbarEnabled = true

            // Set map click listener for location selection
            setOnMapClickListener { latLng ->
                if (isSelectingLocation) {
                    updateUserLocation(latLng)
                    isSelectingLocation = false
                    Toast.makeText(
                        this@LocationActivity,
                        "Location updated!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            // Set marker click listener to show info windows
            setOnMarkerClickListener { marker ->
                marker.showInfoWindow()
                true
            }

            // Set info window click listener to navigate to product detail
            setOnInfoWindowClickListener { marker ->
                val listing = storeMarkers[marker]
                listing?.let {
                    navigateToProductDetail(it)
                }
            }
        }

        // Request location permission
        requestLocationPermission()
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            enableMyLocation()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap?.isMyLocationEnabled = true

            // Load saved location from server first
            loadSavedLocation()
        }
    }

    /**
     * Load user's saved location from server
     */
    private fun loadSavedLocation() {
        lifecycleScope.launch {
            try {
                // Get current user ID (use Alice's ID: 1 as default)
                val consumerId = AuthManager.getCurrentUser()?.userId ?: 1L

                val response = RetrofitClient.apiService.getConsumerLocation(consumerId)

                if (response.isSuccessful) {
                    val location = response.body()
                    if (location != null && location.latitude != null && location.longitude != null) {
                        // Use saved location from server
                        currentLocation = LatLng(location.latitude, location.longitude)
                        android.util.Log.d(
                            "LocationActivity",
                            "Loaded saved location from server: $currentLocation"
                        )
                    } else {
                        // Use default location (Alice's location)
                        currentLocation = DEFAULT_LOCATION
                        android.util.Log.d(
                            "LocationActivity",
                            "No saved location, using default Alice's location: $currentLocation"
                        )
                    }
                } else {
                    // Use default location if API call fails
                    currentLocation = DEFAULT_LOCATION
                    android.util.Log.d(
                        "LocationActivity",
                        "Failed to load location, using default: $currentLocation"
                    )
                }
            } catch (e: Exception) {
                // Use default location on error
                currentLocation = DEFAULT_LOCATION
                android.util.Log.e(
                    "LocationActivity",
                    "Error loading saved location, using default",
                    e
                )
            } finally {
                // Update map and markers after loading location
                updateMapCamera()
                // If listings are already loaded, update the map display
                if (allListings.isNotEmpty()) {
                    displayStoresOnMap()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation()
            } else {
                Toast.makeText(
                    this,
                    "Location permission denied. Using default location.",
                    Toast.LENGTH_SHORT
                ).show()
                currentLocation = DEFAULT_LOCATION
                updateMapCamera()
            }
        }
    }

    private fun loadNearbyListings() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getAllListings()

                if (response.isSuccessful) {
                    allListings = response.body() ?: emptyList()
                    filteredListings = allListings
                    displayStoresOnMap()
                } else {
                    Toast.makeText(
                        this@LocationActivity,
                        "Failed to load stores",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@LocationActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun filterStores(query: String) {
        filteredListings = if (query.isBlank()) {
            allListings
        } else {
            allListings.filter { listing ->
                listing.storeName?.contains(query, ignoreCase = true) == true ||
                listing.title.contains(query, ignoreCase = true) ||
                listing.category?.contains(query, ignoreCase = true) == true ||
                listing.addressLine?.contains(query, ignoreCase = true) == true
            }
        }

        displayStoresOnMap()
    }

    private fun displayStoresOnMap() {
        val map = googleMap ?: return

        // Clear existing markers
        map.clear()
        storeMarkers.clear()

        // Add user's location marker (green)
        map.addMarker(
            MarkerOptions()
                .position(currentLocation)
                .title("Your Location")
                .snippet("Alice's current location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        )

        if (filteredListings.isEmpty()) {
            Toast.makeText(this, "No stores found", Toast.LENGTH_SHORT).show()
            return
        }

        val bounds = LatLngBounds.Builder()
        var hasMarkers = false

        // Include user's location in bounds
        bounds.include(currentLocation)

        // Group listings by store to avoid duplicate markers
        val storeListings = filteredListings.groupBy { it.storeId }

        storeListings.forEach { (_, listings) ->
            val listing = listings.first()

            if (listing.lat != null && listing.lng != null) {
                val position = LatLng(listing.lat, listing.lng)

                val marker = map.addMarker(
                    MarkerOptions()
                        .position(position)
                        .title(listing.storeName ?: "Store")
                        .snippet("${listings.size} item(s) available\n${listing.addressLine ?: ""}")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                )

                marker?.let {
                    storeMarkers[it] = listing
                    bounds.include(position)
                    hasMarkers = true
                }
            }
        }

        // Update camera to show all markers
        if (hasMarkers) {
            try {
                val padding = 100
                val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds.build(), padding)
                map.animateCamera(cameraUpdate)
            } catch (e: Exception) {
                // If bounds are invalid, just move to default location
                updateMapCamera()
            }
        } else {
            updateMapCamera()
        }
    }

    private fun updateMapCamera() {
        googleMap?.moveCamera(
            CameraUpdateFactory.newLatLngZoom(currentLocation, DEFAULT_ZOOM)
        )
    }

    private fun showChangeLocationDialog() {
        val options = arrayOf("Enter Coordinates", "Tap on Map to Select")

        AlertDialog.Builder(this)
            .setTitle("Change Your Location")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showEnterCoordinatesDialog()
                    1 -> enableMapSelectionMode()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEnterCoordinatesDialog() {
        val dialogView = layoutInflater.inflate(android.R.layout.simple_list_item_2, null)
        val latInput = EditText(this).apply {
            hint = "Latitude (e.g., 1.3521)"
            setText(currentLocation.latitude.toString())
        }
        val lngInput = EditText(this).apply {
            hint = "Longitude (e.g., 103.8198)"
            setText(currentLocation.longitude.toString())
        }

        val container = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
            addView(latInput)
            addView(lngInput)
        }

        AlertDialog.Builder(this)
            .setTitle("Enter Coordinates")
            .setView(container)
            .setPositiveButton("Update") { _, _ ->
                val lat = latInput.text.toString().toDoubleOrNull()
                val lng = lngInput.text.toString().toDoubleOrNull()

                if (lat != null && lng != null && lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180) {
                    updateUserLocation(LatLng(lat, lng))
                    Toast.makeText(this, "Location updated!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        this,
                        "Invalid coordinates. Please enter valid latitude (-90 to 90) and longitude (-180 to 180).",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun enableMapSelectionMode() {
        isSelectingLocation = true
        Toast.makeText(
            this,
            "Tap anywhere on the map to set your location",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun updateUserLocation(newLocation: LatLng) {
        currentLocation = newLocation
        displayStoresOnMap()
        updateMapCamera()

        // Save location to server
        saveLocationToServer(newLocation)
    }

    /**
     * Save user's location to the server database
     */
    private fun saveLocationToServer(location: LatLng) {
        lifecycleScope.launch {
            try {
                // Get current user ID (use Alice's ID: 1 as default)
                val consumerId = AuthManager.getCurrentUser()?.userId ?: 1L

                val request = UpdateLocationRequest(
                    latitude = location.latitude,
                    longitude = location.longitude
                )

                val response = RetrofitClient.apiService.updateConsumerLocation(
                    consumerId = consumerId,
                    request = request
                )

                if (response.isSuccessful) {
                    android.util.Log.d(
                        "LocationActivity",
                        "Location saved to server: lat=${location.latitude}, lng=${location.longitude}"
                    )
                } else {
                    android.util.Log.e(
                        "LocationActivity",
                        "Failed to save location: ${response.code()}"
                    )
                    Toast.makeText(
                        this@LocationActivity,
                        "Failed to save location to server",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                android.util.Log.e("LocationActivity", "Error saving location", e)
                Toast.makeText(
                    this@LocationActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // MapView lifecycle methods
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

    /**
     * Navigate to product detail page
     */
    private fun navigateToProductDetail(listing: Listing) {
        val intent = Intent(this, com.example.foodrescuehub.ui.detail.ProductDetailActivity::class.java).apply {
            putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_ID, listing.listingId)
            putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_TITLE, listing.title)
            putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_STORE_NAME, "🏪 ${listing.storeName}")
            putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_CATEGORY, listing.category)
            putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_DISTANCE, "📍 Nearby")
            putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_PRICE, listing.rescuePrice)
            putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_ORIGINAL_PRICE, listing.originalPrice)
            putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_SAVINGS_LABEL, listing.savingsLabel)
            putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_PICKUP_START, listing.pickupStart)
            putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_PICKUP_END, listing.pickupEnd)
            putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_DESCRIPTION, listing.description ?: "")
            putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_QTY_AVAILABLE, listing.qtyAvailable)

            // Pass first photo URL if available
            if (!listing.photoUrls.isNullOrEmpty()) {
                putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_PHOTO_URL, listing.photoUrls[0])
            }
        }
        startActivity(intent)
    }
}
