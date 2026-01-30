package com.example.foodrescuehub

import android.app.Application
import com.example.foodrescuehub.data.repository.AuthManager

class FoodRescueHubApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize AuthManager to restore login state
        AuthManager.initialize(this)
    }
}
