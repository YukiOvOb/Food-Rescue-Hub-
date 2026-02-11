package com.example.foodrescuehub.ui.location

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
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
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val DEFAULT_ZOOM = 13f
        // Default location: Singapore
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
            isSelectingLocation = true
            Toast.makeText(this, "Tap on the map to set your location", Toast.LENGTH_LONG).show()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        googleMap?.apply {
            uiSettings.isZoomControlsEnabled = true
            uiSettings.isMyLocationButtonEnabled = true
            uiSettings.isMapToolbarEnabled = true

            setOnMapClickListener { latLng ->
                if (isSelectingLocation) {
                    updateUserLocation(latLng)
                    isSelectingLocation = false
                }
            }
        }

        requestLocationPermission()
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
                // Call session-based endpoint (no ID required)
                val response = RetrofitClient.apiService.getConsumerLocation()

                if (response.isSuccessful) {
                    val location = response.body()
                    if (location?.latitude != null && location.longitude != null) {
                        currentLocation = LatLng(location.latitude, location.longitude)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                updateMapAndMarkers()
            }
        }
    }

    private fun updateUserLocation(latLng: LatLng) {
        lifecycleScope.launch {
            try {
                val request = UpdateLocationRequest(latLng.latitude, latLng.longitude)
                // Call session-based update (no ID required)
                val response = RetrofitClient.apiService.updateConsumerLocation(request)
                if (response.isSuccessful) {
                    currentLocation = latLng
                    updateMapAndMarkers()
                    Toast.makeText(this@LocationActivity, "Location updated!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
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
        
        map.addMarker(MarkerOptions()
            .position(currentLocation)
            .title("You are here")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)))

        filteredListings.forEach { listing ->
            if (listing.lat != null && listing.lng != null) {
                map.addMarker(MarkerOptions()
                    .position(LatLng(listing.lat, listing.lng))
                    .title(listing.storeName))
            }
        }
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
