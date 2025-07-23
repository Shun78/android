package com.example.ecodeliandroid.network

import okhttp3.Interceptor
import okhttp3.Response
import android.content.Context
import android.content.SharedPreferences

class AuthInterceptor(private val context: Context) : Interceptor {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("ecodeli_prefs", Context.MODE_PRIVATE)

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = prefs.getString("auth_token", null)

        return if (token != null) {
            val newRequest = originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Content-Type", NetworkConfig.CONTENT_TYPE)
                .build()
            chain.proceed(newRequest)
        } else {
            val newRequest = originalRequest.newBuilder()
                .addHeader("Content-Type", NetworkConfig.CONTENT_TYPE)
                .build()
            chain.proceed(newRequest)
        }
    }
}