package com.example.foodrescuehub.data.model

data class ReviewResponse(
    val reviewId: Long,
    val rating: Int,
    val comment: String,
    val user: UserInfo
)

data class UserInfo(
    val name: String
)