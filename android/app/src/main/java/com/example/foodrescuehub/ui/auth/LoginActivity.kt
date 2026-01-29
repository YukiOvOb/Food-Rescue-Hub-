package com.example.foodrescuehub.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foodrescuehub.R
import com.example.foodrescuehub.data.repository.AuthManager
import com.example.foodrescuehub.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            performLogin()
        }

        binding.btnGuest.setOnClickListener {
            // Continue as guest - just close the activity
            finish()
        }
    }

    private fun performLogin() {
        // Get input values
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        // Clear previous errors
        binding.tilEmail.error = null
        binding.tilPassword.error = null

        // Validate email
        if (email.isEmpty()) {
            binding.tilEmail.error = getString(R.string.invalid_email)
            return
        }

        // Validate password
        if (password.length < 6) {
            binding.tilPassword.error = getString(R.string.password_too_short)
            return
        }

        // Show loading
        showLoading(true)

        // Simulate network delay
        binding.root.postDelayed({
            val success = AuthManager.login(email, password)

            showLoading(false)

            if (success) {
                Toast.makeText(this, R.string.login_success, Toast.LENGTH_SHORT).show()
                finish()
            } else {
                binding.tilEmail.error = getString(R.string.invalid_email)
            }
        }, 1000) // 1 second delay to simulate network
    }

    private fun showLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !loading
        binding.btnGuest.isEnabled = !loading
        binding.etEmail.isEnabled = !loading
        binding.etPassword.isEnabled = !loading
    }
}
