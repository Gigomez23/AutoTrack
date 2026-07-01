package ni.edu.uam.autotrak.data.remote.model.sync

import java.time.LocalDate
import java.time.LocalDateTime

data class LicenciaSyncDTO(
    val id: Long? = null,
    val fechaCreacion: LocalDateTime? = null,
    val fechaActualizacion: LocalDateTime? = null,
    val fechaEmitida: LocalDate? = null,
    val fechaVencimiento: LocalDate? = null,
    val imagen: String? = "",
    val categorias: List<String> = emptyList(),
    val usuarioId: Long? = null,
    val eliminado: Boolean = false
)
