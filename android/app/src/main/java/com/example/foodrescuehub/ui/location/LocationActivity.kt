package com.example.foodrescuehub.ui.location

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
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
        private const val TAG = "LocationActivity"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val DEFAULT_ZOOM = 13f
        // Default location: Singapore
        private val DEFAULT_LOCATION = LatLng(1.3521, 103.8198)
    }

    private lateinit var mapView: MapView
    private lateinit var toolbar: MaterialToolbar
    private lateinit var searchView: SearchView
    private lateinit var fabChangeLocation: ExtendedFloatingActionButton
    private lateinit var fabZoomIn: com.google.android.material.floatingactionbutton.FloatingActionButton
    private lateinit var fabZoomOut: com.google.android.material.floatingactionbutton.FloatingActionButton
    private lateinit var fabMyLocation: com.google.android.material.floatingactionbutton.FloatingActionButton
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var googleMap: GoogleMap? = null
    private var currentLocation: LatLng = DEFAULT_LOCATION
    private var tempNewLocation: LatLng? = null  // Temporary location for confirmation
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
        setupMapControlButtons()
        setupBottomNavigation()

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
        fabZoomIn = findViewById(R.id.fabZoomIn)
        fabZoomOut = findViewById(R.id.fabZoomOut)
        fabMyLocation = findViewById(R.id.fabMyLocation)
    }

    private fun setupToolbar() {
        // Toolbar setup without back button
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_location
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(android.content.Intent(this, com.example.foodrescuehub.ui.home.HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_location -> true  // Already on this page
                R.id.nav_orders -> {
                    startActivity(android.content.Intent(this, com.example.foodrescuehub.ui.orders.OrdersActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    startActivity(android.content.Intent(this, com.example.foodrescuehub.ui.profile.ProfileActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
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
            // Show initial instruction dialog
            AlertDialog.Builder(this)
                .setTitle("Change Location")
                .setMessage("Please select your location on the map")
                .setPositiveButton("OK") { dialog, _ ->
                    isSelectingLocation = true
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun setupMapControlButtons() {
        // Zoom In Button
        fabZoomIn.setOnClickListener {
            googleMap?.animateCamera(CameraUpdateFactory.zoomIn())
        }

        // Zoom Out Button
        fabZoomOut.setOnClickListener {
            googleMap?.animateCamera(CameraUpdateFactory.zoomOut())
        }

        // My Location Button - Move to database saved location
        fabMyLocation.setOnClickListener {
            googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, DEFAULT_ZOOM))
            Toast.makeText(this, "Centered on your saved location", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        googleMap?.apply {
            uiSettings.isZoomControlsEnabled = true
            // Disable My Location button - use database location instead
            uiSettings.isMyLocationButtonEnabled = false
            uiSettings.isMapToolbarEnabled = true

            // Move zoom controls to top-right corner
            // setPadding(left, top, right, bottom)
            setPadding(0, 200, 50, 0)

            setOnMapClickListener { latLng ->
                if (isSelectingLocation) {
                    // Save temporary location and show preview
                    tempNewLocation = latLng
                    showLocationPreview(latLng)
                    isSelectingLocation = false
                }
            }

            // Handle marker info window clicks to open listing detail
            setOnInfoWindowClickListener { marker ->
                val listing = storeMarkers[marker]
                if (listing != null) {
                    openListingDetail(listing)
                }
            }
        }

        // Load saved location from database
        loadSavedLocation()
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap?.isMyLocationEnabled = true
            loadSavedLocation()
        }
    }

    private fun loadSavedLocation() {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Loading saved location from database...")
                // Call session-based endpoint (no ID required)
                val response = RetrofitClient.apiService.getConsumerLocation()

                Log.d(TAG, "Response code: ${response.code()}")
                if (response.isSuccessful) {
                    val location = response.body()
                    Log.d(TAG, "Location response: lat=${location?.latitude}, lng=${location?.longitude}")
                    if (location?.latitude != null && location.longitude != null) {
                        currentLocation = LatLng(location.latitude, location.longitude)
                        Log.d(TAG, "Loaded location from database: $currentLocation")
                    } else {
                        Log.d(TAG, "No location in database, using default location")
                    }
                } else {
                    Log.e(TAG, "Failed to load location: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading location", e)
                e.printStackTrace()
            } finally {
                Log.d(TAG, "Final location: $currentLocation")
                // Always update map, using either saved location or default location
                updateMapAndMarkers()
            }
        }
    }

    private fun showLocationPreview(newLocation: LatLng) {
        // Temporarily display the marker at new location
        displayPreviewLocation(newLocation)

        // Show confirmation dialog
        AlertDialog.Builder(this)
            .setTitle("Confirm Location")
            .setMessage("Are you sure you want to change your location?")
            .setPositiveButton("Confirm") { dialog, _ ->
                // User confirmed - update location in database
                updateUserLocation(newLocation)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                // User cancelled - restore original location
                tempNewLocation = null
                displayStoresOnMap()  // Restore original marker
                Toast.makeText(this, "Location change cancelled", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setCancelable(false)  // Prevent dismissing by clicking outside
            .show()
    }

    private fun displayPreviewLocation(newLocation: LatLng) {
        val map = googleMap ?: return
        map.clear()
        storeMarkers.clear()

        // Show green marker at new location
        map.addMarker(MarkerOptions()
            .position(newLocation)
            .title("New location")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)))

        // Display stores
        filteredListings.forEach { listing ->
            if (listing.lat != null && listing.lng != null) {
                val marker = map.addMarker(MarkerOptions()
                    .position(LatLng(listing.lat, listing.lng))
                    .title(listing.storeName))

                // Store the marker-listing relationship
                if (marker != null) {
                    storeMarkers[marker] = listing
                }
            }
        }

        // Move camera to new location
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(newLocation, DEFAULT_ZOOM))
    }

    private fun updateUserLocation(latLng: LatLng) {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Updating location to: $latLng")
                val request = UpdateLocationRequest(latLng.latitude, latLng.longitude)
                // Call session-based update (no ID required)
                val response = RetrofitClient.apiService.updateConsumerLocation(request)
                Log.d(TAG, "Update response code: ${response.code()}")
                if (response.isSuccessful) {
                    currentLocation = latLng
                    Log.d(TAG, "Location updated successfully in database")
                    updateMapAndMarkers()
                    Toast.makeText(this@LocationActivity, "Location updated!", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e(TAG, "Failed to update location: ${response.errorBody()?.string()}")
                    Toast.makeText(this@LocationActivity, "Failed to update location", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating location", e)
                Toast.makeText(this@LocationActivity, "Failed to update location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateMapAndMarkers() {
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, DEFAULT_ZOOM))
        displayStoresOnMap()
    }

    private fun loadNearbyListings() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getAllListings()
                if (response.isSuccessful) {
                    allListings = response.body() ?: emptyList()
                    filteredListings = allListings
                    displayStoresOnMap()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun filterStores(query: String) {
        filteredListings = if (query.isBlank()) {
            allListings
        } else {
            allListings.filter { it.storeName?.contains(query, ignoreCase = true) == true }
        }
        displayStoresOnMap()
    }

    private fun displayStoresOnMap() {
        val map = googleMap ?: return
        map.clear()
        storeMarkers.clear()

        map.addMarker(MarkerOptions()
            .position(currentLocation)
            .title("You are here")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)))

        filteredListings.forEach { listing ->
            if (listing.lat != null && listing.lng != null) {
                val marker = map.addMarker(MarkerOptions()
                    .position(LatLng(listing.lat, listing.lng))
                    .title(listing.storeName))

                // Store the marker-listing relationship for later use
                if (marker != null) {
                    storeMarkers[marker] = listing
                }
            }
        }
    }

    private fun openListingDetail(listing: Listing) {
        val intent = android.content.Intent(this, com.example.foodrescuehub.ui.detail.ProductDetailActivity::class.java).apply {
            putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_ID, listing.listingId)
            putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_TITLE, listing.title)
            putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_STORE_NAME, listing.storeName)
            putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_STORE_ID, listing.storeId)
            putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_CATEGORY, listing.category)
            putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_PRICE, listing.rescuePrice)
            putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_ORIGINAL_PRICE, listing.originalPrice)
            putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_SAVINGS_LABEL, listing.savingsLabel)
            putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_PICKUP_START, listing.pickupStart)
            putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_PICKUP_END, listing.pickupEnd)
            putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_DESCRIPTION, listing.description ?: "")
            putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_QTY_AVAILABLE, listing.qtyAvailable)
            // Get first photo URL if available
            val photoUrl = listing.photoUrls?.firstOrNull()
            if (photoUrl != null) {
                putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_PHOTO_URL, photoUrl)
            }

            // Pass rating data if available
            listing.avgListingAccuracy?.let {
                putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_AVG_ACCURACY, it)
            }
            listing.avgOnTimePickup?.let {
                putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_AVG_ON_TIME, it)
            }
        }
        startActivity(intent)
    }

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
}
