package com.example.sysestoque.backend

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiClient {
    @POST("/clients/register")
    fun registerClient(@Body client: Client): Call<Client>

    @GET("/clients/{ID}")
    fun getClientById(@Path("ID") id: Long): Call<Client>
}