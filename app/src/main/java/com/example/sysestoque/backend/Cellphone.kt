package com.example.sysestoque.backend

data class Cellphone(
    var ddd: Int,
    var number: String,
    var tipo: Char
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Cellphone) return false

        return ddd == other.ddd &&
                number == other.number &&
                tipo == other.tipo
    }

    override fun hashCode(): Int {
        return arrayOf(ddd, number, tipo).contentHashCode()
    }
}