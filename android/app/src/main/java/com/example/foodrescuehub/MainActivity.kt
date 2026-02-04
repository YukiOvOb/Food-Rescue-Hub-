package com.example.foodrescuehub

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.foodrescuehub.data.repository.AuthManager
import com.example.foodrescuehub.ui.auth.LoginActivity

/**
 * Dispatcher Activity that handles initial routing.
 * In this implementation, we force logout on every app start to invalidate the session.
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Force logout on every app start to ensure a fresh session
        AuthManager.logout()
        
        // Redirect to Login
        navigateToLogin()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
