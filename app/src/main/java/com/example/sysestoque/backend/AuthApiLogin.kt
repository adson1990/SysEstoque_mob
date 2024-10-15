package com.example.sysestoque.backend

import retrofit2.Call
import retrofit2.Callback
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("login/client")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @POST("login/client")
    fun login2(@Body loginRequest: LoginRequest): Call<LoginResponseWithSex>
}

data class LoginRequest(val username: String, val password: String)
data class LoginResponse(val token: String)
data class LoginResponseWithSex(
    val accessToken: String,
    val expiresIn: Long,
    val sexo: Char,
    val id: Long
)
