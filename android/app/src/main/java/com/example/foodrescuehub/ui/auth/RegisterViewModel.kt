package com.example.foodrescuehub.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodrescuehub.data.repository.AuthManager
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {

    private val _registerResult = MutableLiveData<Result<Boolean>>()
    val registerResult: LiveData<Result<Boolean>> = _registerResult

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun register(email: String, password: String, displayName: String, phone: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = AuthManager.register(email, password, displayName, phone)
                if (success) {
                    _registerResult.value = Result.success(true)
                } else {
                    _registerResult.value = Result.failure(Exception("Registration failed"))
                }
            } catch (e: Exception) {
                _registerResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
