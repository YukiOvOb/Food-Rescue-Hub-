package com.example.foodrescuehub.data.repository

import com.example.foodrescuehub.data.api.ApiService
import com.example.foodrescuehub.data.model.StoreRecommendation

/**
 * Repository for ML-powered recommendation data operations
 */
class RecommendationRepository(private val apiService: ApiService) {

    /**
     * Get personalized store recommendations for homepage
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
                    Result.failure(Exception(body.message))
                }
            } else {
                Result.failure(Exception("Failed to fetch recommendations: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Search with ML-powered recommendations
     */
    suspend fun searchWithRecommendations(
        consumerId: Long,
        query: String,
        topK: Int = 10,
        lat: Double? = null,
        lng: Double? = null
    ): Result<List<StoreRecommendation>> {
        return try {
            val response = apiService.searchWithRecommendations(consumerId, query, topK, lat, lng)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    Result.success(body.recommendations)
                } else {
                    Result.failure(Exception(body.message))
                }
            } else {
                Result.failure(Exception("Search failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
