package com.example.foodrescuehub.util

import com.example.foodrescuehub.BuildConfig

/**
 * Utility to handle URL formatting for images and API resources
 */
object UrlUtils {

    /**
     * Converts a relative backend path into a full absolute URL
     */
    fun getFullUrl(relativePath: String?): String? {
        if (relativePath == null) return null
        if (relativePath.startsWith("http")) return relativePath
        
        val baseUrl = BuildConfig.BASE_URL.removeSuffix("/")
        val cleanPath = if (relativePath.startsWith("/")) relativePath else "/$relativePath"
        
        return "$baseUrl$cleanPath"
    }
}
