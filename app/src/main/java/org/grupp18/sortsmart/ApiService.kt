package org.grupp18.sortsmart

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ItemRetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8000/"

    val apiService: ItemApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ItemApiService::class.java)
    }
}