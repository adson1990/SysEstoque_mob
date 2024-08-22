package classes.mobile

data class Celphone(
    val id: Int? = null,
    var ddd: Int,
    var number: String,
    var tipo: Char,
    var client: Client? = null
) {
    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        other as Celphone

        if (id != other.id) return false

        return true
    }
}