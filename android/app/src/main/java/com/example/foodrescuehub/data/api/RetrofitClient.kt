package com.example.foodrescuehub.data.api

import android.content.Context
import com.example.foodrescuehub.BuildConfig
import com.example.foodrescuehub.data.storage.SecurePreferences
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton object to configure and provide Retrofit instance
 * Manages the API client configuration with session support
 */
object RetrofitClient {

    // Base URL is now automatically selected based on the build flavor (dev vs prod)
    private val BASE_URL = BuildConfig.BASE_URL
    private var cookieJar: SessionCookieJar? = null

    /**
     * Initialize Retrofit with Context to enable session management
     */
    fun init(context: Context) {
        val securePreferences = SecurePreferences(context)
        cookieJar = SessionCookieJar(securePreferences)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
        
        cookieJar?.let { builder.cookieJar(it) }
        
        builder.build()
    }

    private val gson = GsonBuilder()
        .setLenient()
        .create()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    /**
     * Clear session cookies manually (useful for logout)
     */
    fun clearSession() {
        cookieJar?.clear()
    }
}
