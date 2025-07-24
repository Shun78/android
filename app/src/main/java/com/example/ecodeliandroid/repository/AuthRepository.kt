// app/src/main/java/com/example/ecodeliandroid/repository/AuthRepository.kt
package com.example.ecodeliandroid.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import java.util.*

class AuthRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("ecodeli_prefs", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        Log.d("AuthRepository", "Sauvegarde du nouveau token JWT")
        prefs.edit().putString("auth_token", token).apply()
    }

    fun getToken(): String? {
        val token = prefs.getString("auth_token", null)

        // Vérifier si le token est expiré
        if (token != null && isTokenExpired(token)) {
            Log.w("AuthRepository", "Token JWT expiré, suppression automatique")
            clearToken()
            return null
        }

        return token
    }

    fun clearToken() {
        Log.d("AuthRepository", "Suppression du token JWT")
        prefs.edit().remove("auth_token").apply()
    }

    fun isLoggedIn(): Boolean {
        val token = getToken()
        val isValid = token != null && !isTokenExpired(token)
        Log.d("AuthRepository", "Vérification connexion: $isValid")
        return isValid
    }

    /**
     * Vérifie si un token JWT est expiré
     */
    private fun isTokenExpired(token: String): Boolean {
        return try {
            val payload = decodeJWTPayload(token)
            val exp = payload.optLong("exp", 0)
            val currentTime = System.currentTimeMillis() / 1000

            val isExpired = exp <= currentTime
            if (isExpired) {
                Log.w("AuthRepository", "Token expiré: exp=$exp, current=$currentTime")
            }

            isExpired
        } catch (e: Exception) {
            Log.e("AuthRepository", "Erreur lors de la vérification du token: ${e.message}")
            true // En cas d'erreur, considérer comme expiré
        }
    }

    /**
     * Décode la partie payload d'un JWT (sans vérification de signature)
     */
    private fun decodeJWTPayload(token: String): org.json.JSONObject {
        val parts = token.split(".")
        if (parts.size != 3) {
            throw IllegalArgumentException("Token JWT invalide")
        }

        val payload = parts[1]
        val decodedBytes = Base64.getUrlDecoder().decode(payload)
        val decodedString = String(decodedBytes, Charsets.UTF_8)

        return org.json.JSONObject(decodedString)
    }
}
