package com.example.sysestoque.backend

import java.time.LocalDateTime

data class Product(
    val id: Long,
    val name: String,
    val price: Double,
    val imgUrl: String,
    val description: String,
    val dtIncluded: LocalDateTime,
    val categories: List<CategoryProduct>
)
