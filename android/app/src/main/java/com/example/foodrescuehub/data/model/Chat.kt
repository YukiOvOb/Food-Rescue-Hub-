package com.example.foodrescuehub.data.model

data class ChatRequest(
    val message: String,
    val history: List<HistoryMessage> = emptyList()
)

data class HistoryMessage(
    val role: String, // "user" or "assistant"
    val content: String
)

data class ChatResponse(
    val reply: String
)
