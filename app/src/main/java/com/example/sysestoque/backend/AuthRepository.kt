package com.example.sysestoque.backend

import retrofit2.Retrofit
import retrofit2.Callback
import retrofit2.converter.gson.GsonConverterFactory

class AuthRepository {

    private val authApi: AuthApi

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.10.50.141:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        authApi = retrofit.create(AuthApi::class.java)
    }

    fun login(username: String, password: String, callback: Callback<LoginResponse>) {
        val loginRequest = LoginRequest(username, password)
        val call = authApi.login(loginRequest)
        call.enqueue(callback)
    }
}