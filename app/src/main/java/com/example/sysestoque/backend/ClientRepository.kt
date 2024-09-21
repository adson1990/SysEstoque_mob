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
            .baseUrl("http://10.0.3.2:8080/") // estou usando genymotion, url base do virtualizador é diferente de uma máquina do próprio android studio
            .client(client)
            .addConverterFactory(GsonConverterFactory.create()) // conversor automático de dados Kotlin ou Java em Json
            .build()

        clienteApi = retrofit.create(ClienteApi::class.java) //O Retrofit gera automaticamente o código necessário para fazer requisições HTTP baseadas nos métodos definidos na interface
    }

    fun registerClient(cliente: Client, callback: Callback<Client>) {
        val call = clienteApi.registerClient(cliente)
        call.enqueue(callback)
    }
}