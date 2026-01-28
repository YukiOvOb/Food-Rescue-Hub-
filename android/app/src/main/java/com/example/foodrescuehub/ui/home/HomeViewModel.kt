package com.example.foodrescuehub.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodrescuehub.data.api.RetrofitClient
import com.example.foodrescuehub.data.model.Listing
import com.example.foodrescuehub.data.repository.ListingRepository
import kotlinx.coroutines.launch

/**
 * Sort options for listings
 */
enum class SortOption {
    NAME_ASC,           // A-Z
    NAME_DESC,          // Z-A
    PRICE_LOW_HIGH,     // Price: Low to High
    PRICE_HIGH_LOW,     // Price: High to Low
    POPULARITY,         // Most popular (based on quantity reserved)
    DISTANCE            // Nearest first
}

/**
 * ViewModel for the Home screen
 * Manages UI state and business logic for displaying listings
 */
class HomeViewModel : ViewModel() {

    private val repository = ListingRepository(RetrofitClient.apiService)

    // LiveData for listings
    private val _listings = MutableLiveData<List<Listing>>()
    val listings: LiveData<List<Listing>> = _listings

    // LiveData for filtered listings (after category or search filter)
    private val _filteredListings = MutableLiveData<List<Listing>>()
    val filteredListings: LiveData<List<Listing>> = _filteredListings

    // LiveData for loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData for error messages
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // Current selected category
    private val _selectedCategory = MutableLiveData<String>("All")
    val selectedCategory: LiveData<String> = _selectedCategory

    // Current tab (Mystery Boxes or Regular Items)
    private val _selectedTab = MutableLiveData<Int>(0) // 0: Mystery Boxes, 1: Regular Items
    val selectedTab: LiveData<Int> = _selectedTab

    // Current sort option
    private val _sortOption = MutableLiveData<SortOption>(SortOption.NAME_ASC)
    val sortOption: LiveData<SortOption> = _sortOption

    // Price range filter
    private var minPrice: Double = 0.0
    private var maxPrice: Double = Double.MAX_VALUE

    // Search query
    private var currentSearchQuery: String = ""

    // User location (default: Bukit Timah area)
    private var userLat = 1.3431
    private var userLng = 103.7764
    private var searchRadius = 2.0 // 2 km radius

    init {
        // Load all listings by default (not just nearby)
        loadAllListings()
    }

    /**
     * Load nearby listings based on user location
     */
    fun loadNearbyListings() {
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val result = repository.getNearbyListings(userLat, userLng, searchRadius)
                result.onSuccess { listingList ->
                    _listings.value = listingList
                    applyFilters()
                    _isLoading.value = false
                }.onFailure { exception ->
                    _error.value = exception.message ?: "Unknown error occurred"
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load listings"
                _isLoading.value = false
            }
        }
    }

    /**
     * Load all listings (fallback if location is not available)
     */
    fun loadAllListings() {
        android.util.Log.d("HomeViewModel", "Starting to load all listings")
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                android.util.Log.d("HomeViewModel", "Making API call")
                val result = repository.getAllListings()
                result.onSuccess { listingList ->
                    android.util.Log.d("HomeViewModel", "Successfully loaded ${listingList.size} listings")
                    _listings.value = listingList
                    applyFilters()
                    _isLoading.value = false
                }.onFailure { exception ->
                    android.util.Log.e("HomeViewModel", "Failed to load listings", exception)
                    _error.value = exception.message ?: "Unknown error occurred"
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "Exception loading listings", e)
                _error.value = e.message ?: "Failed to load listings"
                _isLoading.value = false
            }
        }
    }

    /**
     * Filter listings by category
     */
    fun filterByCategory(category: String) {
        _selectedCategory.value = category
        applyFilters()
    }

    /**
     * Switch between Mystery Boxes and Regular Items tabs
     */
    fun selectTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
        applyFilters()
    }

    /**
     * Search listings by store name or item name
     */
    fun searchListings(query: String) {
        currentSearchQuery = query
        applyFilters()
    }

    /**
     * Sort listings by selected option
     */
    fun sortBy(option: SortOption) {
        android.util.Log.d("HomeViewModel", "Sorting by: $option")
        _sortOption.value = option
        applyFilters()
    }

    /**
     * Filter by price range
     */
    fun filterByPriceRange(min: Double, max: Double) {
        android.util.Log.d("HomeViewModel", "Filtering by price: $min - $max")
        minPrice = min
        maxPrice = max
        applyFilters()
    }

    /**
     * Clear all filters
     */
    fun clearFilters() {
        minPrice = 0.0
        maxPrice = Double.MAX_VALUE
        currentSearchQuery = ""
        _selectedCategory.value = "All"
        _sortOption.value = SortOption.NAME_ASC
        applyFilters()
    }

    /**
     * Apply category and tab filters to listings
     */
    private fun applyFilters() {
        val currentListings = _listings.value ?: emptyList()
        val category = _selectedCategory.value ?: "All"
        val tabIndex = _selectedTab.value ?: 0
        val sort = _sortOption.value ?: SortOption.NAME_ASC

        android.util.Log.d("HomeViewModel", "Applying filters - Total: ${currentListings.size}, Category: $category, Tab: $tabIndex, Sort: $sort")

        var filtered = currentListings

        // Filter by search query
        if (currentSearchQuery.isNotBlank()) {
            filtered = filtered.filter {
                it.title.contains(currentSearchQuery, ignoreCase = true) ||
                        it.storeName.contains(currentSearchQuery, ignoreCase = true) ||
                        it.category.contains(currentSearchQuery, ignoreCase = true)
            }
            android.util.Log.d("HomeViewModel", "After search filter: ${filtered.size} listings")
        }

        // Filter by category
        if (category != "All") {
            filtered = filtered.filter { it.category.equals(category, ignoreCase = true) }
            android.util.Log.d("HomeViewModel", "After category filter: ${filtered.size} listings")
        }

        // Filter by price range
        filtered = filtered.filter { it.rescuePrice >= minPrice && it.rescuePrice <= maxPrice }
        android.util.Log.d("HomeViewModel", "After price filter: ${filtered.size} listings")

        // Filter by tab (Mystery Boxes vs Regular Items)
        // For now, show all items in both tabs since we don't have a type flag yet
        filtered = when (tabIndex) {
            0 -> filtered // Mystery Boxes - show all items
            1 -> filtered // Regular Items - show all items for now
            else -> filtered
        }

        // Apply sorting
        filtered = when (sort) {
            SortOption.NAME_ASC -> filtered.sortedBy { it.storeName.lowercase() }
            SortOption.NAME_DESC -> filtered.sortedByDescending { it.storeName.lowercase() }
            SortOption.PRICE_LOW_HIGH -> filtered.sortedBy { it.rescuePrice }
            SortOption.PRICE_HIGH_LOW -> filtered.sortedByDescending { it.rescuePrice }
            SortOption.POPULARITY -> filtered.sortedByDescending { it.qtyReserved }
            SortOption.DISTANCE -> {
                // Sort by distance (placeholder - would use actual user location)
                filtered.sortedBy {
                    calculateDistance(userLat, userLng, it.lat ?: 0.0, it.lng ?: 0.0)
                }
            }
        }

        android.util.Log.d("HomeViewModel", "After sorting: ${filtered.size} listings")
        _filteredListings.value = filtered
    }

    /**
     * Calculate distance between two points (Haversine formula)
     */
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371 // Radius of Earth in kilometers
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }

    /**
     * Update user location
     */
    fun updateLocation(lat: Double, lng: Double, radius: Double = 2.0) {
        userLat = lat
        userLng = lng
        searchRadius = radius
        loadNearbyListings()
    }

    /**
     * Refresh listings
     */
    fun refresh() {
        loadNearbyListings()
    }
}
