package com.example.foodrescuehub.data.api

import com.example.foodrescuehub.data.storage.SecurePreferences
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

/**
 * Custom CookieJar implementation to manage and persist session-based authentication cookies.
 * This ensures that session identifiers like JSESSIONID from a Spring Boot backend are
 * captured and included in all subsequent network requests.
 */
class SessionCookieJar(private val securePreferences: SecurePreferences) : CookieJar {

    /**
     * Captures cookies from an HTTP response and persists them to secure storage.
     * This is typically called when the backend sends a 'Set-Cookie' header,
     * such as during a successful login.
     *
     * @param url The URL that sent the cookies.
     * @param cookies The list of cookies received from the server.
     */
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        if (cookies.isNotEmpty()) {
            // Convert Cookie objects to their string representation for storage
            val cookieStrings = cookies.map { it.toString() }
            securePreferences.saveCookies(cookieStrings)
        }
    }

    /**
     * Loads persisted cookies from secure storage to be included in an outgoing HTTP request.
     * This ensures that the session is maintained across different API calls and app restarts.
     *
     * @param url The URL for which cookies are being requested.
     * @return A list of cookies to be added to the request headers.
     */
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val cookieStrings = securePreferences.getCookies()
        // Re-parse the stored strings into OkHttp Cookie objects
        return cookieStrings.mapNotNull { Cookie.parse(url, it) }
    }

    /**
     * Clears all persisted cookies and session data.
     * This should be called during the logout process to ensure the user session is
     * completely removed from the device.
     */
    fun clear() {
        // securePreferences.clearUser() also handles removing session cookies
        securePreferences.clearUser()
    }
}
