package com.example.foodrescuehub.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.foodrescuehub.data.api.RetrofitClient
import com.example.foodrescuehub.data.model.LoginRequest
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
     * On success, the backend session (JSESSIONID) is automatically captured
     * and persisted by the SessionCookieJar.
     *
     * @return true if login successful, false otherwise
     */
    suspend fun login(email: String, password: String): Boolean {
        // Basic local validation
        if (!isValidEmail(email) || password.length < 6) {
            return false
        }

        return try {
            val response = RetrofitClient.apiService.login(LoginRequest(email, password))
            
            if (response.isSuccessful && response.body() != null) {
                val user = response.body()!!
                
                // Save user to encrypted storage
                securePreferences.saveUser(user)

                // Update LiveData (use postValue as this might be on a background thread)
                _currentUser.postValue(user)
                _isLoggedIn.postValue(true)
                
                // Synchronize cart state after successful login
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
     * Clears user data, session cookies, and local cart
     */
    fun logout() {
        // Clear user and cookies from storage
        securePreferences.clearUser()
        
        // Clear cookies from the active Retrofit client
        RetrofitClient.clearSession()

        // Update LiveData
        _currentUser.value = null
        _isLoggedIn.value = false

        // Clear cart on logout
        CartManager.clearCartForLogout()
    }

    /**
     * Check if user is currently logged in
     * @return true if logged in, false otherwise
     */
    fun isUserLoggedIn(): Boolean {
        return _isLoggedIn.value == true
    }

    /**
     * Get current user
     * @return User object if logged in, null otherwise
     */
    fun getCurrentUser(): User? {
        return _currentUser.value
    }

    /**
     * Validate email format using regex
     */
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return email.matches(emailRegex)
    }
}
