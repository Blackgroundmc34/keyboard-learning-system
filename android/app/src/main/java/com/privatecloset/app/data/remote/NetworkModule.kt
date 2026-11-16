package com.privatecloset.app.data.remote

import com.privatecloset.app.data.local.TokenManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Handles network client configuration, dynamic host switching, JWT token injection,
 * and Retrofit setup.
 */
class NetworkModule(private val tokenManager: TokenManager) {

    /**
     * Interceptor that adds the Authorization header when a valid token is stored.
     */
    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val token = tokenManager.getToken()

        val request = if (!token.isNullOrBlank()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/json")
                .build()
        } else {
            originalRequest.newBuilder()
                .header("Accept", "application/json")
                .build()
        }

        chain.proceed(request)
    }

    /**
     * Interceptor that dynamically swaps the host, scheme, and port to match the currently
     * configured backend base URL from [TokenManager].
     */
    private val dynamicHostInterceptor = Interceptor { chain ->
        var request = chain.request()
        val configuredUrlStr = tokenManager.getBaseUrl()
        val configuredHttpUrl = configuredUrlStr.toHttpUrlOrNull()

        if (configuredHttpUrl != null) {
            val newHttpUrl = request.url.newBuilder()
                .scheme(configuredHttpUrl.scheme)
                .host(configuredHttpUrl.host)
                .port(configuredHttpUrl.port)
                .build()
            request = request.newBuilder().url(newHttpUrl).build()
        }

        chain.proceed(request)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(dynamicHostInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(6, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    val apiService: ApiService by lazy {
        val initialUrl = tokenManager.getBaseUrl()
        Retrofit.Builder()
            .baseUrl(initialUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ApiService::class.java)
    }
}
