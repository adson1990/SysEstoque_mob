package com.example.sysestoque.backend

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.Callback
import retrofit2.converter.gson.GsonConverterFactory

class ClientRepository() {

    private val clienteApi: ClienteApi

    init {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        val client = OkHttpClient.Builder()
            .addInterceptor(logging) // Adiciona o interceptador
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.3.2:8080/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        clienteApi = retrofit.create(ClienteApi::class.java)
    }

    fun registerClient(cliente: Client, callback: Callback<Client>) {
        val call = clienteApi.registerClient(cliente)
        call.enqueue(callback)
    }
}