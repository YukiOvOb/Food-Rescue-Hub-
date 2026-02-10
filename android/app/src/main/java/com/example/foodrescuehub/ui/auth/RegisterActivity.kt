package com.example.foodrescuehub.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.foodrescuehub.R
import com.example.foodrescuehub.databinding.ActivityRegisterBinding
import com.example.foodrescuehub.ui.home.HomeActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(this) { showLoading(it) }

        viewModel.registerResult.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, R.string.register_success, Toast.LENGTH_SHORT).show()
                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }.onFailure {
                Toast.makeText(this, it.message ?: "Registration failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupListeners() {
        binding.btnRegister.setOnClickListener { performRegister() }
        binding.btnBackToLogin.setOnClickListener {
            finish()
        }
    }

    private fun performRegister() {
        val email = binding.etEmail.text.toString().trim()
        val displayName = binding.etDisplayName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirm = binding.etConfirmPassword.text.toString()

        binding.tilEmail.error = null
        binding.tilPassword.error = null
        binding.tilConfirmPassword.error = null

        if (email.isEmpty()) {
            binding.tilEmail.error = getString(R.string.invalid_email)
            return
        }
        if (password.length < 8) {
            binding.tilPassword.error = getString(R.string.password_too_short)
            return
        }
        if (password != confirm) {
            binding.tilConfirmPassword.error = getString(R.string.password_mismatch)
            return
        }

        val safeDisplay = if (displayName.isBlank()) email.substringBefore("@", email) else displayName
        val safePhone = phone.ifBlank { null }
        viewModel.register(email, password, safeDisplay, safePhone)
    }

    private fun showLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !loading
        binding.btnBackToLogin.isEnabled = !loading
        binding.etEmail.isEnabled = !loading
        binding.etDisplayName.isEnabled = !loading
        binding.etPhone.isEnabled = !loading
        binding.etPassword.isEnabled = !loading
        binding.etConfirmPassword.isEnabled = !loading
    }
}
