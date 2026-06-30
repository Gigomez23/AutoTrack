package ni.edu.uam.autotrak.data.remote.model

import java.time.LocalDate

data class Licencia(
    override val id: Long? = null,
    override val fechaEmitida: LocalDate? = null,
    override val fechaVencimiento: LocalDate? = null,
//    override val imagen: String = "",
    val usuario: Usuario? = null
) : Documento
