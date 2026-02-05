package com.example.foodrescuehub.data.api

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton object to configure and provide Retrofit instance
 * Manages the API client configuration
 */
object RetrofitClient {

    // Backend server URL
    // For Android emulator connecting to localhost: "http://10.0.2.2:8081/"
    // For remote EC2 server: "http://47.129.223.141:8081/"
    private const val BASE_URL = "http://10.0.2.2:8081/"

//  Chatbot Backend (Port 8000)
    private const val BOT_BASE_URL = "http://10.0.2.2:8000/"

    // Logging interceptor for debugging API calls
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // OkHttp client with timeout and logging configurations
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // Gson instance with custom configurations
    private val gson = GsonBuilder()
        .setLenient()
        .create()

    // Retrofit instance
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    // API service instance
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    // --- THE CHATBOT BACKEND ---
    private val botRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BOT_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val rescueBotApi: RescueBotApi by lazy {
        botRetrofit.create(RescueBotApi::class.java)
    }

    /**
     * Update the base URL dynamically if needed
     * Useful for switching between development and production servers
     */
    fun updateBaseUrl(newBaseUrl: String): ApiService {
        return Retrofit.Builder()
            .baseUrl(newBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}
