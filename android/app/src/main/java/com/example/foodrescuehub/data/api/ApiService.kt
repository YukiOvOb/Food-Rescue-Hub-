package com.example.foodrescuehub.data.api

import com.example.foodrescuehub.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API interface for Food Rescue Hub backend
 * Uses session-based authentication (JSESSIONID cookie)
 */
interface ApiService {

    // ==================== AUTH ====================

    @POST("api/auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<User>

    @POST("api/auth/logout")
    suspend fun logout(): Response<Void>

    // ==================== LISTINGS ====================

    @GET("api/listings")
    suspend fun getAllListings(): Response<List<Listing>>

    @GET("api/listings/{listingId}")
    suspend fun getListingById(@Path("listingId") listingId: Long): Response<Listing>

    @GET("api/listings/nearby")
    suspend fun getNearbyListings(
        @Query("lat") latitude: Double,
        @Query("lng") longitude: Double,
        @Query("radius") radius: Double = 5.0
    ): Response<List<Listing>>

    @GET("api/listings/category/{category}")
    suspend fun getListingsByCategory(
        @Path("category") category: String
    ): Response<List<Listing>>

    // ==================== CART ====================

    @GET("api/cart")
    suspend fun getCart(): Response<CartResponseDto>

    @POST("api/cart/items")
    suspend fun addItemToCart(@Body request: AddCartItemRequest): Response<CartResponseDto>

    @PATCH("api/cart/items/{listingId}")
    suspend fun updateCartItemQuantity(
        @Path("listingId") listingId: Long, 
        @Body request: UpdateCartItemRequest
    ): Response<CartResponseDto>

    @DELETE("api/cart/items/{listingId}")
    suspend fun removeCartItem(@Path("listingId") listingId: Long): Response<CartResponseDto>

    @DELETE("api/cart/items")
    suspend fun clearCart(): Response<CartResponseDto>

    // ==================== ORDERS ====================

    /**
     * Create order
     * Updated to return the compact CreateOrderResponseDto as per latest backend contract.
     */
    @POST("api/orders")
    suspend fun createOrder(
        @Query("pickupSlotStart") pickupSlotStart: String,
        @Query("pickupSlotEnd") pickupSlotEnd: String,
    ): Response<CreateOrderResponseDto>

    @GET("api/orders/consumer")
    suspend fun getMyOrders(): Response<List<Order>>

    @GET("api/orders/{orderId}")
    suspend fun getOrderById(@Path("orderId") orderId: Long): Response<Order>

    @PATCH("api/orders/{orderId}/cancel")
    suspend fun cancelOrder(
        @Path("orderId") orderId: Long,
        @Query("cancelReason") cancelReason: String? = null
    ): Response<Order>

    @PATCH("api/orders/{orderId}/status")
    suspend fun updateOrderStatus(
        @Path("orderId") orderId: Long,
        @Query("status") status: String
    ): Response<Order>

    // ==================== CONSUMER PROFILE ====================

    @PUT("api/consumer/profile/location")
    suspend fun updateConsumerLocation(@Body request: UpdateLocationRequest): Response<UpdateLocationRequest>

    @GET("api/consumer/profile/location")
    suspend fun getConsumerLocation(): Response<UpdateLocationRequest>

    // ==================== PICKUP ====================

    @POST("api/pickup-tokens/{orderId}/generate-qrcode")
    suspend fun generatePickupQRCode(@Path("orderId") orderId: Long): Response<Map<String, String>>
}
