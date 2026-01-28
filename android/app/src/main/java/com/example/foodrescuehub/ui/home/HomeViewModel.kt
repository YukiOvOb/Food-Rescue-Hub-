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
        if (query.isBlank()) {
            applyFilters()
            return
        }

        val currentListings = _listings.value ?: emptyList()
        val searchResults = currentListings.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.storeName.contains(query, ignoreCase = true)
        }
        _filteredListings.value = searchResults
    }

    /**
     * Apply category and tab filters to listings
     */
    private fun applyFilters() {
        val currentListings = _listings.value ?: emptyList()
        val category = _selectedCategory.value ?: "All"
        val tabIndex = _selectedTab.value ?: 0

        android.util.Log.d("HomeViewModel", "Applying filters - Total listings: ${currentListings.size}, Category: $category, Tab: $tabIndex")

        var filtered = currentListings

        // Filter by category
        if (category != "All") {
            filtered = filtered.filter { it.category.equals(category, ignoreCase = true) }
            android.util.Log.d("HomeViewModel", "After category filter: ${filtered.size} listings")
        }

        // Filter by tab (Mystery Boxes vs Regular Items)
        // For now, show all items in both tabs since we don't have a type flag yet
        filtered = when (tabIndex) {
            0 -> filtered // Mystery Boxes - show all items
            1 -> filtered // Regular Items - show all items for now
            else -> filtered
        }

        android.util.Log.d("HomeViewModel", "After tab filter: ${filtered.size} listings")
        _filteredListings.value = filtered
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
