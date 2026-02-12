package com.example.foodrescuehub.ui.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.foodrescuehub.R
import com.example.foodrescuehub.databinding.ActivityLoginBinding
import com.example.foodrescuehub.ui.home.HomeActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }


        viewModel.loginResult.observe(this) { result ->
            result.onSuccess { user -> // named return outcome user
                Toast.makeText(this, R.string.login_success, Toast.LENGTH_SHORT).show()


                // save User ID  for ReviewActivity

                val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
                with (sharedPref.edit()) {

                    try {

                        putLong("KEY_USER_ID", user.userId)
                    } catch (e: Exception) {
                        // 如果实在读不到，存个默认值防止崩坏
                        putLong("KEY_USER_ID", 1L)
                    }

                    apply()
                }
                // ---------------------------------------------------------

                // jump to home page
                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)

            }.onFailure {
                binding.tilEmail.error = getString(R.string.invalid_email)
                Toast.makeText(this, it.message ?: "Login failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            performLogin()
        }

        binding.btnGuest.setOnClickListener {
            // guest model
            val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
            with (sharedPref.edit()) {
                putLong("KEY_USER_ID", 999L) // 999 behave guest
                apply()
            }

            // Guests also go to HomeActivity
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun performLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        binding.tilEmail.error = null
        binding.tilPassword.error = null

        if (email.isEmpty()) {
            binding.tilEmail.error = getString(R.string.invalid_email)
            return
        }

        if (password.length < 6) {
            binding.tilPassword.error = getString(R.string.password_too_short)
            return
        }

        viewModel.login(email, password)
    }

    private fun showLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !loading
        binding.btnGuest.isEnabled = !loading
        binding.etEmail.isEnabled = !loading
        binding.etPassword.isEnabled = !loading
    }
}