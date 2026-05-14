package org.grupp18.sortsmart.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ItemRetrofitClient {

    const val BASE_URL = "https://sortsmart.kleopatra.pro/"
    val apiService: ItemApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ItemApiService::class.java)
    }
}