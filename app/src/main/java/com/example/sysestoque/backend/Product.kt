package com.example.sysestoque.backend

import java.time.LocalDateTime

data class Product(
    val id: Long,
    val nome: String,
    val preco: Double,
    val imgUrl: String,
    val descricao: String,
    val dtIncluded: LocalDateTime,
    val categories: List<CategoryProduct>
)
