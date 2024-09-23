package com.example.sysestoque.backend

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthApiEmail {
    @GET("email/{email}")
    fun searchLogin(@Path("email") email: String): Call<Client>

    @POST("token/consulta")
    fun getToken(@Body request: TokenRequest): Call<TokenResponse>
}

data class TokenRequest(
    val username: String
)

data class TokenResponse(
    val accessToken: String,
    val expiresIn: Long
)