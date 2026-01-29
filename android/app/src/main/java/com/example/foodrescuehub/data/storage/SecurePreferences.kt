package com.example.foodrescuehub.data.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.foodrescuehub.data.model.User
import com.google.gson.Gson

class SecurePreferences(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_FILENAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val gson = Gson()

    /**
     * Save user data to encrypted storage
     */
    fun saveUser(user: User) {
        val userJson = gson.toJson(user)
        sharedPreferences.edit()
            .putString(KEY_USER, userJson)
            .apply()
    }

    /**
     * Retrieve user data from encrypted storage
     * Returns null if no user is saved
     */
    fun getUser(): User? {
        val userJson = sharedPreferences.getString(KEY_USER, null) ?: return null
        return try {
            gson.fromJson(userJson, User::class.java)
        } catch (e: Exception) {
            // Handle corrupted data
            clearUser()
            null
        }
    }

    /**
     * Clear user data from encrypted storage
     */
    fun clearUser() {
        sharedPreferences.edit()
            .remove(KEY_USER)
            .apply()
    }

    companion object {
        private const val PREFS_FILENAME = "food_rescue_hub_secure_prefs"
        private const val KEY_USER = "user_data"
    }
}
