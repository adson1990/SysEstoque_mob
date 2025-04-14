package com.example.sysestoque.backend

import com.google.gson.annotations.SerializedName

data class ItensCompra(
    @SerializedName("productName")
    val nomeProduto: String,

    @SerializedName("description")
    val descricao: String,

    val quantidade: Int,

    @SerializedName("prcUnitario")
    val prcUnitario: Double,

    @SerializedName("categoryNames")
    val categorias: String // Agora é String, separada por vírgulas
)
