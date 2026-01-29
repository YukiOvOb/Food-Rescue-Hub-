package com.example.foodrescuehub.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.foodrescuehub.data.model.User
import com.example.foodrescuehub.data.storage.SecurePreferences
import kotlin.random.Random

/**
 * Singleton manager for user authentication
 * Manages login state, current user, and persistence
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
     * Perform login with email and password
     * Current implementation: Mock login (accepts any email + password length >= 6)
     *
     * @return true if login successful, false otherwise
     */
    fun login(email: String, password: String): Boolean {
        // Validate email format
        if (!isValidEmail(email)) {
            return false
        }

        // Validate password length
        if (password.length < 6) {
            return false
        }

        // Mock login: Generate a user object
        val user = User(
            userId = Random.nextLong(1000, 999999),
            email = email,
            displayName = extractDisplayName(email),
            phone = null,
            createdAt = System.currentTimeMillis()
        )

        // Save user to encrypted storage
        securePreferences.saveUser(user)

        // Update LiveData
        _currentUser.value = user
        _isLoggedIn.value = true

        return true
    }

    /**
     * Logout the current user
     * Clears user data and cart
     */
    fun logout() {
        // Clear user from storage
        securePreferences.clearUser()

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

    /**
     * Extract display name from email (part before @)
     */
    private fun extractDisplayName(email: String): String {
        val username = email.substringBefore("@")
        // Capitalize first letter
        return username.replaceFirstChar { it.uppercase() }
    }
}
