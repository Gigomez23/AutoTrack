package ni.edu.uam.autotrak.data.remote.model

import java.time.LocalDate

interface Registro {
    val id: Long?
    val fechaRegistro: LocalDate?
    val nota: String?
}

data class RegistroGeneral(
    override val id: Long? = null,
    override val fechaRegistro: LocalDate? = null,
    override val nota: String? = "",
) : Registro
