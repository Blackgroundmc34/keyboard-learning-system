package com.privatecloset.app.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages JWT Auth token, user credentials, and configurable backend URL in SharedPreferences.
 */
class TokenManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _tokenFlow = MutableStateFlow<String?>(prefs.getString(KEY_TOKEN, null))
    val tokenFlow: StateFlow<String?> = _tokenFlow.asStateFlow()

    private val _baseUrlFlow = MutableStateFlow(
        prefs.getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
    )
    val baseUrlFlow: StateFlow<String> = _baseUrlFlow.asStateFlow()

    fun saveToken(token: String?) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
        _tokenFlow.value = token
    }

    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun clearToken() {
        prefs.edit().remove(KEY_TOKEN).apply()
        _tokenFlow.value = null
    }

    fun saveUserInfo(id: String?, name: String?, email: String?, role: String?) {
        prefs.edit()
            .putString(KEY_USER_ID, id)
            .putString(KEY_USER_NAME, name)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_ROLE, role)
            .apply()
    }

    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)
    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, null)
    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)
    fun getUserRole(): String? = prefs.getString(KEY_USER_ROLE, "STUDENT")

    fun setBaseUrl(url: String) {
        val sanitized = sanitizeUrl(url)
        prefs.edit().putString(KEY_BASE_URL, sanitized).apply()
        _baseUrlFlow.value = sanitized
    }

    fun getBaseUrl(): String {
        return prefs.getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
    }

    fun clearAll() {
        prefs.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_NAME)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_USER_ROLE)
            .apply()
        _tokenFlow.value = null
    }

    private fun sanitizeUrl(url: String): String {
        var trimmed = url.trim()
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            trimmed = "http://$trimmed"
        }
        if (!trimmed.endsWith("/")) {
            trimmed = "$trimmed/"
        }
        return trimmed
    }

    companion object {
        private const val PREFS_NAME = "private_closet_auth_prefs"
        private const val KEY_TOKEN = "jwt_auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_BASE_URL = "backend_base_url"

        // Uses BACKEND_BASE_URL configured in app/build.gradle.kts (overrideable in-app)
        val DEFAULT_BASE_URL: String
            get() = com.privatecloset.app.BuildConfig.BACKEND_BASE_URL
    }
}
