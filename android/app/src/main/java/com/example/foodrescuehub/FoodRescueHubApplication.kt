package com.example.foodrescuehub

import android.app.Application
import com.example.foodrescuehub.data.api.RetrofitClient
import com.example.foodrescuehub.data.repository.AuthManager

class FoodRescueHubApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // Initialize Retrofit with session support
        RetrofitClient.init(this)
        
        // Initialize AuthManager to restore login state
        AuthManager.initialize(this)
    }
}
