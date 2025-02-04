package com.softylasa.smartengineer.network

import okhttp3.Interceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.UUID
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://app.rudhashi.xyz/apps/api/"

    // Generates a random User-Agent to make requests look like they come from different devices
    private fun getRandomUserAgent(): String {
        val deviceId = UUID.randomUUID().toString().substring(0, 8)  // Random device ID
        return "Mozilla/5.0 (Linux; Android 11; Device-$deviceId) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Mobile Safari/537.36"
    }

    // Interceptor to add dynamic headers (User-Agent, etc.)
    private val headerInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .header("User-Agent", getRandomUserAgent()) // Spoof user agent
            .header("Accept", "application/json") // Ensure JSON responses
            .build()
        chain.proceed(request)
    }

    // Logging Interceptor (for debugging)
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // OkHttpClient with security, headers, and connection pooling
    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)  // Retry on failure
            .addInterceptor(headerInterceptor) // Add custom headers
            .addInterceptor(loggingInterceptor) // Debugging logs
            .build()
    }

    // Retrofit instance (Thread-safe Singleton)
    private val retrofit by lazy {
        synchronized(this) { // Ensuring thread safety
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
    }

    // API Service instance
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

}
