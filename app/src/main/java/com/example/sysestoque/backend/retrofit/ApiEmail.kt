package com.example.sysestoque.backend.retrofit

import com.example.sysestoque.backend.Client
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiEmail {
    @GET("clients/email/{email}")
    fun searchEmail(@Path("email") email: String): Call<Long>

    @PUT("clients/password/new/{ID}")
    fun newPassword(
        @Path("ID") id: Long,
        @Body request: PassRequest
    ): Call<PassResponse>
}

data class PassRequest(
    val newPassword: String,
    val idClient: Long
)
data class PassResponse(
    val cliente: Client
)