package com.example.sysestoque.backend

data class Compra(
    val codPrd: String,
    val nomePrd: String,
    val codCli: String,
    val qtd: Int,
    val dataVenda: String,
    val total: Double,
    val prcUnitario: Double
)
