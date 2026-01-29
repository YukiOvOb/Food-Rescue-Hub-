package com.example.foodrescuehub.data.model

/**
 * Data class representing a listing item
 * Matches the backend ListingDTO structure
 */
data class Listing(
    val listingId: Long,
    val title: String,
    val description: String?,
    val originalPrice: Double,
    val rescuePrice: Double,
    val pickupStart: String, // ISO 8601 format from backend
    val pickupEnd: String,
    val expiryAt: String,
    val status: String,

    // Store information
    val storeId: Long,
    val storeName: String,
    val storeDescription: String?,
    val addressLine: String,
    val postalCode: String?,
    val lat: Double?,
    val lng: Double?,
    val pickupInstructions: String?,
    val openingHours: String?,

    // Store category
    val category: String,

    // Inventory
    val qtyAvailable: Int,
    val qtyReserved: Int,

    // Photos
    val photoUrls: List<String>?,

    // Calculated fields
    val timeRemaining: String,
    val savingsAmount: Double,
    val savingsLabel: String
)
