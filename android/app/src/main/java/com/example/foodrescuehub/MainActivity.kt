package com.example.foodrescuehub

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.foodrescuehub.data.api.RetrofitClient
import com.example.foodrescuehub.data.repository.AuthManager
import com.example.foodrescuehub.ui.auth.LoginActivity
import com.example.foodrescuehub.ui.home.HomeActivity
import kotlinx.coroutines.launch

/**
 * Dispatcher Activity that handles initial routing.
 * Checks for existing sessions and redirects the user to either Login or Home.
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Check local login state
        if (!AuthManager.isUserLoggedIn()) {
            navigateToLogin()
        } else {
            // 2. Validate session with backend
            validateSessionAndNavigate()
        }
    }

    private fun validateSessionAndNavigate() {
        lifecycleScope.launch {
            try {
                // Try a simple authenticated request
                val response = RetrofitClient.apiService.getAllListings()
                
                if (response.isSuccessful) {
                    // Session is valid
                    navigateToHome()
                } else if (response.code() == 401) {
                    // Session expired
                    AuthManager.logout()
                    navigateToLogin()
                } else {
                    // Other server error - proceed to home anyway 
                    // and let individual screens handle failures
                    navigateToHome()
                }
            } catch (e: Exception) {
                // Network error - proceed to home (offline mode support)
                navigateToHome()
            }
        }
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}
