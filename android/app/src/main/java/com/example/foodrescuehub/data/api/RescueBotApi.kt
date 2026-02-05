package com.example.foodrescuehub.data.api

import com.example.foodrescuehub.data.model.ChatRequest
import com.example.foodrescuehub.data.model.ChatResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface RescueBotApi {
    @POST("/chat")
    suspend fun sendMessage(@Body request: ChatRequest): Response<ChatResponse>
}