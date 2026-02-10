package com.example.foodrescuehub.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodrescuehub.data.model.User
import com.example.foodrescuehub.data.repository.AuthManager
import kotlinx.coroutines.launch

/**
 * ViewModel for LoginActivity
 * Manages UI state and coordinates with AuthManager for authentication
 */
class LoginViewModel : ViewModel() {

    private val _loginResult = MutableLiveData<Result<User>>()
    val loginResult: LiveData<Result<User>> = _loginResult

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = AuthManager.login(email, password)

                if (success) {
                    // getCurrentUser
                    var user = AuthManager.getCurrentUser()

                    // if null create a casual user
                    if (user == null) {

                        user = User(
                            userId = 1L,
                            email = email,
                            displayName = "Temp User",
                            phone = null               // optional
                        )
                    }

                    _loginResult.value = Result.success(user)
                } else {
                    _loginResult.value = Result.failure(Exception("Login failed"))
                }
            } catch (e: Exception) {
                _loginResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}