package com.example.foodrescuehub.data.api

import com.example.foodrescuehub.data.model.Listing
import com.example.foodrescuehub.data.model.Order
import com.example.foodrescuehub.data.model.UpdateLocationRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API interface for Food Rescue Hub backend
 * Defines all API endpoints for listing and order operations
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
     * Get all orders for a specific consumer
     * GET /api/consumer/orders/{consumerId}
     */
    @GET("api/consumer/orders/{consumerId}")
    suspend fun getConsumerOrders(
        @Path("consumerId") consumerId: Long
    ): Response<List<Order>>

    /**
     * Get a specific order by ID
     * GET /api/consumer/orders/{consumerId}/order/{orderId}
     */
    @GET("api/consumer/orders/{consumerId}/order/{orderId}")
    suspend fun getOrderById(
        @Path("consumerId") consumerId: Long,
        @Path("orderId") orderId: Long
    ): Response<Order>

    /**
     * Update consumer's default location
     * PUT /api/consumer/profile/{consumerId}/location
     */
    @PUT("api/consumer/profile/{consumerId}/location")
    suspend fun updateConsumerLocation(
        @Path("consumerId") consumerId: Long,
        @Body request: UpdateLocationRequest
    ): Response<UpdateLocationRequest>

    /**
     * Get consumer's default location
     * GET /api/consumer/profile/{consumerId}/location
     */
    @GET("api/consumer/profile/{consumerId}/location")
    suspend fun getConsumerLocation(
        @Path("consumerId") consumerId: Long
    ): Response<UpdateLocationRequest>

    /**
     * Generate QR code for order pickup
     * POST /api/pickup-tokens/{orderId}/generate-qrcode
     */
    @POST("api/pickup-tokens/{orderId}/generate-qrcode")
    suspend fun generatePickupQRCode(
        @Path("orderId") orderId: Long
    ): Response<Map<String, String>>
}
