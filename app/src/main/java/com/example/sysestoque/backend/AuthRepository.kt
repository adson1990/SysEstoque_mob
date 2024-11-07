package com.example.sysestoque.backend

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.Callback
import retrofit2.converter.gson.GsonConverterFactory

class AuthRepository {

    val authApi: ApiLogin

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.3.2:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        authApi = retrofit.create(ApiLogin::class.java)
    }

    fun requestToken(token: String): Retrofit{
        val retrofitWithToken = Retrofit.Builder()
            .baseUrl("http://10.0.3.2:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient.Builder().addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                    .header("Authorization", "Bearer $token")  // Adiciona o token no cabeçalho
                    .method(original.method, original.body)
                val requestWithToken = requestBuilder.build()
                chain.proceed(requestWithToken)
            }.build())
            .build()

        return retrofitWithToken;
    }

    fun getTokenByEmail(email: String, callback: Callback<TokenResponse>){
        val request = TokenRequest(email)
        val call = authApi.getToken(request)
        call.enqueue(callback)
    }

    fun getRefreshToken(refresh: String, callback: Callback<TokenRefreshResponse>){
        val request = TokenRefreshRequest(refresh)
        val call = authApi.refreshToken(request)
        call.enqueue(callback)
    }

    fun getTemporaryToken(email: String, callback: Callback<TokenResponse>){
        val request = TokenRequest(email)
        val call = authApi.temporaryToken(request)
        call.enqueue(callback)
    }
}
