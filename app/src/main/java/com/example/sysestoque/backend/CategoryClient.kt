package com.example.sysestoque.backend

data class CategoryClient(
    val id: Long? = null,
    var description: String
) {
    val clients: MutableSet<Client> = HashSet()

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        other as CategoryClient

        if (id != other.id) return false

        return true
    }
}