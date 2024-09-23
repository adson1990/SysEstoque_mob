package com.example.sysestoque.backend

import android.os.Looper
import android.os.Handler
import android.util.Log
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

    fun validaEmail(email: String, token: String, callback: Callback<Client>) {
        val emailCall = authApiEmail.searchLogin(email)
        emailCall.enqueue(object : Callback<Client> {
            override fun onResponse(call: Call<Client>, response: Response<Client>) {
                callback.onResponse(call, response)
            }

            override fun onFailure(call: Call<Client>, t: Throwable) {
                callback.onFailure(call, t)
            }
        })
    }
}
