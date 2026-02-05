package com.example.foodrescuehub.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foodrescuehub.R
import com.example.foodrescuehub.data.repository.AuthManager
import com.example.foodrescuehub.databinding.ActivityProfileBinding
import com.example.foodrescuehub.ui.auth.LoginActivity
import com.example.foodrescuehub.ui.chatbot.ChatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupListeners()
        observeAuthState()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupListeners() {
        // Login button
        binding.btnLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        // Logout button
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }

        // Menu items - show "Coming soon" toast
        binding.cvMyOrders.setOnClickListener {
            Toast.makeText(this, R.string.feature_coming_soon, Toast.LENGTH_SHORT).show()
        }

        binding.cvSavedStores.setOnClickListener {
            Toast.makeText(this, R.string.feature_coming_soon, Toast.LENGTH_SHORT).show()
        }

        binding.cvSettings.setOnClickListener {
            Toast.makeText(this, R.string.feature_coming_soon, Toast.LENGTH_SHORT).show()
        }

        binding.cvHelp.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }
    }

    private fun observeAuthState() {
        // Observe current user
        AuthManager.currentUser.observe(this) { user ->
            if (user != null) {
                // User is logged in
                binding.cvUserInfo.visibility = View.VISIBLE
                binding.cvGuestMessage.visibility = View.GONE
                binding.btnLogin.visibility = View.GONE
                binding.btnLogout.visibility = View.VISIBLE

                // Display user information
                binding.tvDisplayName.text = user.displayName
                binding.tvEmail.text = user.email

                // Format member since date
                val dateFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
                val memberSince = dateFormat.format(Date(user.createdAt))
                binding.tvMemberSince.text = getString(R.string.member_since, memberSince)
            } else {
                // User is not logged in (guest)
                binding.cvUserInfo.visibility = View.GONE
                binding.cvGuestMessage.visibility = View.VISIBLE
                binding.btnLogin.visibility = View.VISIBLE
                binding.btnLogout.visibility = View.GONE
            }
        }
    }

    private fun showLogoutConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.logout_confirm_title)
            .setMessage(R.string.logout_confirm_message)
            .setPositiveButton(R.string.logout) { dialog, _ ->
                AuthManager.logout()
                Toast.makeText(this, R.string.logout_success, Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
