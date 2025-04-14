package com.example.sysestoque.backend.retrofit

import com.example.sysestoque.backend.Compras
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiCompra {
    @GET("compras/cliente/{id}")
    suspend fun getComprasPorCliente(@Path("id") id: Long): Response<List<Compras>>
}

data class ComprasResponse(
    val content: List<Compras>
)