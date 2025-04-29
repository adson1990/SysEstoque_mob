package com.example.sysestoque.backend.retrofit

import com.example.sysestoque.backend.Compras
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import java.math.BigDecimal
import java.util.Date

interface ApiCompra {
    @GET("compras/cliente/{id}")
    suspend fun getComprasPorCliente(@Path("id") id: Long): Response<List<Compras>>

    @GET("compras/ultimas-compras/{id}")
    fun getUltimasCompras(@Path("id") id: Long, @Header("Authorization") token: String): Call<List<UltimaCompra>>

    @GET("compras/ultimas-compras-order-valor/{id}")
    fun getUltimasComprasPorValor(@Path("id") id: Long, @Header("Authorization") token: String): Call<List<UltimaCompra>>
}

data class UltimaCompra(
    @SerializedName("dataVenda") val dataVenda: Date,
    @SerializedName("valor") val valor: BigDecimal
)