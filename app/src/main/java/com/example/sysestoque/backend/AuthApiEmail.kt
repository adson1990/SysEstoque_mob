package com.example.sysestoque.backend

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AuthApiEmail {
    @GET("clients/email/{email}")
    fun searchEmail(@Path("email") email: String): Call<Long>

    @POST("token/consulta")
    fun getToken(@Body request: TokenRequest): Call<TokenResponse>

    @PUT("clients/password/new/{ID}")
    fun newPassword(
        @Path("ID") id: Long,
        @Body request: PassRequest
    ): Call<PassResponse>
}

data class TokenRequest(
    val username: String
)
data class TokenResponse(
    val accessToken: String,
    val expiresInSeconds: Long
)

data class PassRequest(
    val newPassword: String,
    val idClient: Long
)
data class PassResponse(
    val cliente: Client
)