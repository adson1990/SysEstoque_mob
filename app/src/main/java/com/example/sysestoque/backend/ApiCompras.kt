package com.example.sysestoque.backend

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.Date

interface ApiCompras{

    @GET("/vendas/client/{id}")
    fun getComprasPorId(@Path("id") id : Long): Call<ComprasResponse>
}

data class ComprasResponse(
    val content: List<Compras>
)

data class Compras(
    val name: String,
    val dataVenda: Date,
    val valor: Double
)