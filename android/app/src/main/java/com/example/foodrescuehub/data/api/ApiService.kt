package com.example.foodrescuehub.data.api

import com.example.foodrescuehub.data.model.Listing
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API interface for Food Rescue Hub backend
 * Defines all API endpoints for listing operations
 */
interface ApiService {

    /**
     * Get all active listings
     * GET /api/listings
     */
    @GET("api/listings")
    suspend fun getAllListings(): Response<List<Listing>>

    /**
     * Get nearby listings based on location
     * GET /api/listings/nearby?lat={lat}&lng={lng}&radius={radius}
     */
    @GET("api/listings/nearby")
    suspend fun getNearbyListings(
        @Query("lat") latitude: Double,
        @Query("lng") longitude: Double,
        @Query("radius") radius: Double = 5.0
    ): Response<List<Listing>>

    /**
     * Get listings filtered by category
     * GET /api/listings/category/{category}
     */
    @GET("api/listings/category/{category}")
    suspend fun getListingsByCategory(
        @Path("category") category: String
    ): Response<List<Listing>>
}
