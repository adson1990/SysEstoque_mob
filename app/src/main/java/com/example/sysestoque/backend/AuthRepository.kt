package com.example.sysestoque.backend

import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.Callback
import retrofit2.Response
import retrofit2.converter.gson.GsonConverterFactory

class AuthRepository {

    private val authApi: AuthApi
    private val authApiEmail: AuthApiEmail


    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.3.2:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        authApi = retrofit.create(AuthApi::class.java)
        authApiEmail = retrofit.create(AuthApiEmail::class.java)
    }

    fun getToken(username: String, callback: Callback<TokenResponse>) {
        val request = TokenRequest(username)
        val call = authApiEmail.getToken(request)
        call.enqueue(callback)
    }

    fun login(username: String, password: String, callback: Callback<LoginResponse>) {
        val loginRequest = LoginRequest(username, password)
        val call = authApi.login(loginRequest)
        call.enqueue(callback)
    }

    fun validaEmail(email: String, token: String, callback: Callback<Long>) {
      //  val request = EmailRequest(email)

        // retrofit personalizado para esta requisição, já que o token precisa ir junto
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

        val authApiEmailWithToken = retrofitWithToken.create(AuthApiEmail::class.java)

        // Agora, faz a chamada com o Retrofit que inclui o token no header
        val emailCall = authApiEmailWithToken.searchEmail(email)
        emailCall.enqueue(object : Callback<Long> {
            override fun onResponse(call: Call<Long>, response: Response<Long>) {
                callback.onResponse(call, response)
            }

            override fun onFailure(call: Call<Long>, t: Throwable) {
                callback.onFailure(call, t)
            }
        })
    }
}
