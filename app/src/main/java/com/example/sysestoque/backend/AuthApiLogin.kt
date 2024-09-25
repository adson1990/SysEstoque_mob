package com.example.sysestoque.backend

import retrofit2.Call
import retrofit2.Callback
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("/login")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>
}

data class LoginRequest(val username: String, val password: String)
data class LoginResponse(val token: String)