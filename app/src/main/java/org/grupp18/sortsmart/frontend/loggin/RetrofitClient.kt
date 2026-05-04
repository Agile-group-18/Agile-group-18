package org.grupp18.sortsmart.frontend.loggin

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Singleton Retrofit client.
 * Access [RetrofitClient.api] anywhere in the app.
 */
object RetrofitClient {

    private const val BASE_URL = "https://sortsmart.kleopatra.pro/api/v1/"

    // Holds the access token for authenticated requests
    var accessToken: String? = null

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            val requestBuilder = originalRequest.newBuilder()

            // Attach Bearer token to every request if available
            accessToken?.let {
                requestBuilder.addHeader("Authorization", "Bearer $it")
            }

            chain.proceed(requestBuilder.build())
        }
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}