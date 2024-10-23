package com.example.sysestoque.backend

data class Client(
    val name: String,
    val cpf: String,
    val income: Double,
    val birthDate: String,
    val sexo: Char,
    val email: String,
    val senha: String,
    //val foto: ByteArray, para salvar em um campo BLOB
    val foto: String,
    val cellphones: List<Cellphone>,
    val enderecos: List<Enderecos>
)
