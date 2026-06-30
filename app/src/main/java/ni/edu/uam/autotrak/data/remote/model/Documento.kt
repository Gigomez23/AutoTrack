package ni.edu.uam.autotrak.data.remote.model

import java.time.LocalDate
import java.time.LocalDateTime

interface Documento {
    val id: Long?
    val fechaEmitida: LocalDate?
    val fechaVencimiento: LocalDate?
}

data class DocumentoGeneral(
    override val id: Long? = null,
    override val fechaEmitida: LocalDate? = null,
    override val fechaVencimiento: LocalDate? = null,
) : Documento
