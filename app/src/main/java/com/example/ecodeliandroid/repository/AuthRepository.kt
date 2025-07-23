// app/src/main/java/com/example/ecodeliandroid/repository/AuthRepository.kt
package com.example.ecodeliandroid.repository

import android.content.Context
import android.content.SharedPreferences

class AuthRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("ecodeli_prefs", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString("auth_token", token).apply()
    }

    fun getToken(): String? {
        return prefs.getString("auth_token", null)
    }

    fun clearToken() {
        prefs.edit().remove("auth_token").apply()
    }

    fun isLoggedIn(): Boolean {
        return getToken() != null
    }
}
