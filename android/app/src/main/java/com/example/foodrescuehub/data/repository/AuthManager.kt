package com.example.foodrescuehub.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.foodrescuehub.data.api.RetrofitClient
import com.example.foodrescuehub.data.model.LoginRequest
import com.example.foodrescuehub.data.model.RegisterRequest
import com.example.foodrescuehub.data.model.User
import com.example.foodrescuehub.data.storage.SecurePreferences

/**
 * Singleton manager for user authentication
 * Manages login state, current user, and persistence with backend session support
 */
object AuthManager {

    private lateinit var securePreferences: SecurePreferences

    private val _isLoggedIn = MutableLiveData<Boolean>(false)
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    private val _currentUser = MutableLiveData<User?>(null)
    val currentUser: LiveData<User?> = _currentUser

    /**
     * Initialize AuthManager and restore login state from storage
     * Should be called once in Application.onCreate()
     */
    fun initialize(context: Context) {
        securePreferences = SecurePreferences(context.applicationContext)

        // Restore user from storage
        val savedUser = securePreferences.getUser()
        if (savedUser != null) {
            _currentUser.value = savedUser
            _isLoggedIn.value = true
        }
    }

    /**
     * Perform login with email and password against the Spring Boot backend.
     */
    suspend fun login(email: String, password: String): Boolean {
        if (!isValidEmail(email) || password.length < 6) {
            return false
        }

        return try {
            val response = RetrofitClient.apiService.login(LoginRequest(email, password))
            
            if (response.isSuccessful && response.body() != null) {
                val user = response.body()!!
                
                // Save user to encrypted storage
                securePreferences.saveUser(user)

                // Update state immediately
                _currentUser.postValue(user)
                _isLoggedIn.postValue(true)
                
                CartManager.fetchCart()
                
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Register a new consumer account and log in immediately.
     */
    suspend fun register(email: String, password: String, displayName: String, phone: String? = null): Boolean {
        if (!isValidEmail(email) || password.length < 8) {
            return false
        }

        val effectiveDisplayName = displayName.ifBlank {
            email.substringBefore("@", email)
        }

        return try {
            val payload = RegisterRequest(
                email = email,
                password = password,
                displayName = effectiveDisplayName,
                phone = phone?.takeIf { it.isNotBlank() },
                role = "CONSUMER"
            )

            val response = RetrofitClient.apiService.register(payload)
            if (response.isSuccessful && response.body() != null) {
                val user = response.body()!!
                securePreferences.saveUser(user)
                _currentUser.postValue(user)
                _isLoggedIn.postValue(true)
                CartManager.fetchCart()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Logout the current user
     */
    fun logout() {
        securePreferences.clearUser()
        RetrofitClient.clearSession()
        _currentUser.value = null
        _isLoggedIn.value = false
        CartManager.clearCartForLogout()
    }

    /**
     * Check if user is currently logged in
     */
    fun isUserLoggedIn(): Boolean {
        // Use preference as source of truth to avoid LiveData lag
        return securePreferences.getUser() != null
    }

    /**
     * Get the current user ID
     */
    fun getUserId(): Long {
        return securePreferences.getUserId()
    }

    /**
     * Get current user
     */
    fun getCurrentUser(): User? {
        return _currentUser.value ?: securePreferences.getUser()
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return email.matches(emailRegex)
    }
}
