package com.example.foodrescuehub.data.storage

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.foodrescuehub.data.model.User
import com.google.gson.Gson
import java.io.File

class SecurePreferences(context: Context) {

    private val sharedPreferences: SharedPreferences = createSharedPreferences(context)
    private val gson = Gson()

    private fun createSharedPreferences(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return try {
            EncryptedSharedPreferences.create(
                context,
                PREFS_FILENAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e("SecurePreferences", "Error creating EncryptedSharedPreferences, resetting...", e)
            // If creation fails (e.g. AEADBadTagException), clear the corrupted file and try again
            deleteSharedPreferences(context)
            EncryptedSharedPreferences.create(
                context,
                PREFS_FILENAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
    }

    private fun deleteSharedPreferences(context: Context) {
        try {
            // Delete the physical XML file
            val sharedPrefsDir = File(context.filesDir.parent, "shared_prefs")
            val prefsFile = File(sharedPrefsDir, "$PREFS_FILENAME.xml")
            if (prefsFile.exists()) {
                prefsFile.delete()
            }
        } catch (e: Exception) {
            Log.e("SecurePreferences", "Failed to delete corrupted prefs file", e)
        }
    }

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
        val userJson = try {
            sharedPreferences.getString(KEY_USER, null)
        } catch (e: Exception) {
            Log.e("SecurePreferences", "Error reading user data", e)
            null
        } ?: return null
        
        return try {
            gson.fromJson(userJson, User::class.java)
        } catch (e: Exception) {
            // Handle corrupted data
            clearUser()
            null
        }
    }

    /**
     * Get the current user's ID
     */
    fun getUserId(): Long {
        return getUser()?.userId ?: 0L
    }

    /**
     * Clear user data and session from encrypted storage
     */
    fun clearUser() {
        sharedPreferences.edit()
            .remove(KEY_USER)
            .remove(KEY_COOKIES)
            .apply()
    }

    /**
     * Save session cookies
     */
    fun saveCookies(cookies: List<String>) {
        sharedPreferences.edit()
            .putStringSet(KEY_COOKIES, cookies.toSet())
            .apply()
    }

    /**
     * Retrieve session cookies
     */
    fun getCookies(): List<String> {
        return try {
            sharedPreferences.getStringSet(KEY_COOKIES, emptySet())?.toList() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    companion object {
        private const val PREFS_FILENAME = "food_rescue_hub_secure_prefs"
        private const val KEY_USER = "user_data"
        private const val KEY_COOKIES = "session_cookies"
    }
}
