package com.example.sysestoque.backend

data class Cellphone(
    var ddd: Int,
    var number: String,
    var tipo: Char
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        other as Cellphone

        return true
    }

    override fun hashCode(): Int {
        return number.hashCode()
    }
}