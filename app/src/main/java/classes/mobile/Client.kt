package classes.mobile

import java.time.Instant

data class Client(
    val id: Long? = null,
    val name: String,
    val cpf: String,
    val income: Double,
    val birthDate: Instant,
    val sexo: Char,
    val email: String,
    val senha: String
)
