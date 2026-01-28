package com.example.foodrescuehub.data.repository

import com.example.foodrescuehub.data.api.ApiService
import com.example.foodrescuehub.data.model.Listing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository class to handle data operations
 * Acts as a single source of truth for listing data
 */
class ListingRepository(private val apiService: ApiService) {

    /**
     * Fetch all active listings from the backend
     */
    suspend fun getAllListings(): Result<List<Listing>> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("ListingRepository", "Fetching all listings from API")
            val response = apiService.getAllListings()
            android.util.Log.d("ListingRepository", "Response code: ${response.code()}, Success: ${response.isSuccessful}")

            if (response.isSuccessful && response.body() != null) {
                val listings = response.body()!!
                android.util.Log.d("ListingRepository", "Received ${listings.size} listings")
                listings.forEachIndexed { index, listing ->
                    android.util.Log.d("ListingRepository", "  $index: ${listing.title} - ${listing.storeName}")
                }
                Result.success(listings)
            } else {
                android.util.Log.e("ListingRepository", "Failed with code: ${response.code()}, message: ${response.message()}")
                Result.failure(Exception("Failed to fetch listings: ${response.code()}"))
            }
        } catch (e: Exception) {
            android.util.Log.e("ListingRepository", "Exception fetching listings", e)
            Result.failure(e)
        }
    }

    /**
     * Fetch nearby listings based on user location
     */
    suspend fun getNearbyListings(
        latitude: Double,
        longitude: Double,
        radius: Double = 5.0
    ): Result<List<Listing>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getNearbyListings(latitude, longitude, radius)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch nearby listings: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetch listings filtered by category
     */
    suspend fun getListingsByCategory(category: String): Result<List<Listing>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getListingsByCategory(category)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch listings by category: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
