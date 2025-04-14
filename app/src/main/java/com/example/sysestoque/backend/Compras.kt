package com.example.sysestoque.backend

import com.google.gson.annotations.SerializedName

data class Compras(
    @SerializedName("clientName")
    val nomeCliente: String,

    @SerializedName("dataCompra")
    val dataCompra: String,

    @SerializedName("priceTotal")
    val precoTotal: Double,

    @SerializedName("itens")
    val itens: List<ItensCompra>
)