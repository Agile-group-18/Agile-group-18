package org.grupp18.sortsmart.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://sortsmart.kleopatra.pro/api/v1/"

    val apiService: RecyclingApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RecyclingApiService::class.java)
    }
}