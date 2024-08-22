package classes.mobile

data class Enderecos(
    val id: Int? = null,
    var rua: String,
    var bairro: String,
    var num: Int,
    var estado: String,
    var country: String,
    var cep: String,
    var client: Client? = null
) {
    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        other as Enderecos

        if (id != other.id) return false

        return true
    }
}