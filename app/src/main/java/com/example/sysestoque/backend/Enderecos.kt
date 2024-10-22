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
        if (other !is Enderecos) return false

        return rua == other.rua &&
                bairro == other.bairro &&
                num == other.num &&
                cidade == other.cidade &&
                estado == other.estado &&
                country == other.country &&
                cep == other.cep
    }

    override fun hashCode(): Int {
        return arrayOf(rua, bairro, num, cidade, estado, country, cep).contentHashCode()
    }
}