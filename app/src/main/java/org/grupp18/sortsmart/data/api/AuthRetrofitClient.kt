package org.grupp18.sortsmart.data.api

import android.content.Context
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import org.grupp18.sortsmart.data.api.dto.RefreshRequest
import org.grupp18.sortsmart.data.local.SortSmartDatabase
import org.grupp18.sortsmart.data.local.entity.TokenEntity
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Singleton Retrofit client.
 * Access [AuthRetrofitClient.api] anywhere in the app.
 * Automatically refreshes the access token using the stored refresh token.
 */
object AuthRetrofitClient {

    private const val BASE_URL = "https://sortsmart.kleopatra.pro/api/v1/"

    var accessToken: String? = null
    private var refreshToken: String? = null
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    // Save both tokens to memory and database
    suspend fun saveTokens(access: String, refresh: String) {
        accessToken = access
        refreshToken = refresh
        appContext?.let { ctx ->
            val db = SortSmartDatabase.getInstance(ctx)
            db.tokenDao().saveTokens(TokenEntity(accessToken = access, refreshToken = refresh))
        }
    }

    // Load tokens from database on app start
    suspend fun loadTokensFromDb(): Boolean {
        val ctx = appContext ?: return false
        val db = SortSmartDatabase.getInstance(ctx)
        val tokens = db.tokenDao().getTokens() ?: return false
        accessToken = tokens.accessToken
        refreshToken = tokens.refreshToken
        return true
    }

    // Clear tokens from memory and database on logout
    suspend fun clearTokens() {
        accessToken = null
        refreshToken = null
        appContext?.let { ctx ->
            val db = SortSmartDatabase.getInstance(ctx)
            db.tokenDao().clearTokens()
        }
    }

    // Try to refresh the access token using the refresh token
    private suspend fun tryRefresh(): Boolean {
        val refresh = refreshToken ?: return false
        return try {
            val response = api.refresh(RefreshRequest(refreshToken = refresh))
            if (response.isSuccessful) {
                response.body()?.let { auth ->
                    saveTokens(auth.accessToken, auth.refreshToken)
                    true
                } ?: false
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    private val tokenAuthenticator = Authenticator { _: Route?, response: Response ->
        // If this is already a retry, give up to avoid infinite loop
        if (response.request.header("X-Retry-After-Refresh") != null) return@Authenticator null

        val refreshed = runBlocking { tryRefresh() }
        if (!refreshed) {
            runBlocking { clearTokens() }
            return@Authenticator null
        }

        response.request.newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .header("X-Retry-After-Refresh", "true")
            .build()
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder().apply {
                accessToken?.let { addHeader("Authorization", "Bearer $it") }
            }.build()
            chain.proceed(request)
        }
        .authenticator(tokenAuthenticator)
        .build()

    val api: AuthApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApiService::class.java)
    }
}