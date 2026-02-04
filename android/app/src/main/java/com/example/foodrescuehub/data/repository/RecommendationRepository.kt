package com.example.foodrescuehub.data.repository

import com.example.foodrescuehub.data.api.ApiService
import com.example.foodrescuehub.data.model.StoreRecommendation

/**
 * Repository for recommendation-related data operations
 */
class RecommendationRepository(private val apiService: ApiService) {

    /**
     * Get personalized store recommendations for homepage
     *
     * @param consumerId User ID
     * @param topK Number of recommendations to fetch (default 5)
     * @param lat User latitude
     * @param lng User longitude
     * @return Result with list of recommendations
     */
    suspend fun getHomePageRecommendations(
        consumerId: Long,
        topK: Int = 5,
        lat: Double? = null,
        lng: Double? = null
    ): Result<List<StoreRecommendation>> {
        return try {
            val response = apiService.getHomePageRecommendations(consumerId, topK, lat, lng)

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    Result.success(body.recommendations)
                } else {
                    Result.failure(Exception("API returned success=false: ${body.message}"))
                }
            } else {
                Result.failure(Exception("Failed to fetch recommendations: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
