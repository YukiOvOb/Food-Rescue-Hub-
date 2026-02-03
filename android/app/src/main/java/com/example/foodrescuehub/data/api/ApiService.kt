package com.example.foodrescuehub.data.api

import com.example.foodrescuehub.data.model.ConsumerProfile
import com.example.foodrescuehub.data.model.Listing
import com.example.foodrescuehub.data.model.RecommendationResponse
import com.example.foodrescuehub.data.model.StoreRecommendation
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

    /**
     * Get personalized store recommendations for homepage
     * GET /api/recommendations/homepage?consumerId={consumerId}&topK={topK}&lat={lat}&lng={lng}
     */
    @GET("api/recommendations/homepage")
    suspend fun getHomePageRecommendations(
        @Query("consumerId") consumerId: Long,
        @Query("topK") topK: Int = 5,
        @Query("lat") latitude: Double? = null,
        @Query("lng") longitude: Double? = null
    ): Response<RecommendationResponse>

    /**
     * Search with ML-powered recommendations
     * GET /api/recommendations/search?consumerId={consumerId}&query={query}&topK={topK}&lat={lat}&lng={lng}
     */
    @GET("api/recommendations/search")
    suspend fun searchWithRecommendations(
        @Query("consumerId") consumerId: Long,
        @Query("query") query: String,
        @Query("topK") topK: Int = 10,
        @Query("lat") latitude: Double? = null,
        @Query("lng") longitude: Double? = null
    ): Response<RecommendationResponse>

    /**
     * Get consumer profile by ID
     * GET /api/consumer/{consumerId}
     */
    @GET("api/consumer/{consumerId}")
    suspend fun getConsumerProfile(
        @Path("consumerId") consumerId: Long
    ): Response<ConsumerProfile>

    /**
     * Get consumer profile by email
     * GET /api/consumer/by-email?email={email}
     */
    @GET("api/consumer/by-email")
    suspend fun getConsumerProfileByEmail(
        @Query("email") email: String
    ): Response<ConsumerProfile>
}
