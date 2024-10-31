package com.example.sysestoque.backend

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiLogin {
    @POST("login/client")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @POST("token/cliente")
    fun getToken(@Body request: TokenRequest): Call<TokenResponse>

   // fun refreshToken(@Body tokenRefresh: String):
}

data class TokenRequest(
    val username: String
)
data class TokenResponse(
    val accessToken: String,
    val expiresInSeconds: Long
)

data class LoginRequest(val username: String, val password: String)
data class LoginResponse(val accessToken: String,
                         val expiresIn: Long,
                         val sexo: Char,
                         val id: Long,
                         val foto: String)