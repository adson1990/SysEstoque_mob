package com.example.sysestoque.backend

import retrofit2.Retrofit
import retrofit2.Callback
import retrofit2.converter.gson.GsonConverterFactory

class ClientRepository {

    private val clienteApi: ClienteApi

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://localhost:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        clienteApi = retrofit.create(ClienteApi::class.java)
    }

    fun cadastrarCliente(cliente: Client, celphones: List<Celphone>, enderecos: List<Enderecos>, callback: Callback<Client>) {
        val call = clienteApi.cadastrarCliente(cliente)
        call.enqueue(callback)
    }
}