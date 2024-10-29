package com.example.sysestoque.backend

data class CategoryProduct(
    val id: Long,
    val description: String,
    val products: List<Product>
)
