package com.example.sysestoque.backend

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiClient {
    @POST("/clients/register")
    fun registerClient(@Body client: Client): Call<Client>

    @GET("/clients/{ID}")
    fun getClientById(@Path("ID") id: Long,@Header("Authorization") token: String): Call<Client>

    @PATCH("/clients/upd/{ID}")
    fun updateClientWithToken(
        @Path("ID") id: Long,
        @Body client: Client,
        @Header("Authorization") token: String
    ): Call<Client>
}