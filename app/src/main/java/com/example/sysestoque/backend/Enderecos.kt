package com.example.sysestoque.backend

data class Enderecos(

    var rua: String,
    var bairro: String,
    var num: Int,
    var cidade: String,
    var estado: String,
    var country: String,
    var cep: String
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        other as Enderecos

        return true
    }
}