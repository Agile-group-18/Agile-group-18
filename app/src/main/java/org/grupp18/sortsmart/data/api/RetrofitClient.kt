package org.grupp18.sortsmart.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "https://sortsmart.kleopatra.pro/api/v1/"

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val stationService: RecyclingApiService by lazy {
        retrofit.create(RecyclingApiService::class.java)
    }

    val itemService: ItemApiService by lazy {
        retrofit.create(ItemApiService::class.java)
    }
}