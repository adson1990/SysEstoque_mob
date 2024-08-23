package com.example.sysestoque.backend

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ClienteApi {
    @POST("clients")
    fun cadastrarCliente(@Body client: Client): Call<Client>
}