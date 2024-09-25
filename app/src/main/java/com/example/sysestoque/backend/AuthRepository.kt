package com.example.sysestoque.backend

import android.util.Log
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

    fun setNewPassword(password: String, id: Long, token: String, callback: Callback<PassResponse>){

        val retrofitWithToken = Retrofit.Builder()
            .baseUrl("http://10.0.3.2:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient.Builder().addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .method(original.method, original.body)
                val requestWithToken = requestBuilder.build()
                chain.proceed(requestWithToken)
            }.build())
            .build()

        val authApiEmailWithToken = retrofitWithToken.create(AuthApiEmail::class.java)

        val passRequest = PassRequest(password, id)

        val passCall = authApiEmailWithToken.newPassword(id, passRequest)
        passCall.enqueue(object : Callback<PassResponse> {
            override fun onResponse(call: Call<PassResponse>, response: Response<PassResponse>) {
                if (response.isSuccessful) {
                    callback.onResponse(call, response)
                } else {
                    Log.e("FailureConnection","A senha do cliente não será alterada! Falha na comunicação")
                    callback.onFailure(call, Throwable("Erro ao alterar a senha"))
                }
            }

            override fun onFailure(call: Call<PassResponse>, t: Throwable) {
                callback.onFailure(call, t)
            }
        })
    }
}
