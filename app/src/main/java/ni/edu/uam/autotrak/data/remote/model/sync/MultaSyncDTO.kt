package ni.edu.uam.autotrak.data.remote.model.sync

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

data class MultaSyncDTO(
    val id: Long? = null,
    val fechaCreacion: LocalDateTime? = null,
    val fechaActualizacion: LocalDateTime? = null,
    val fechaVencimiento: LocalDate? = null,
    val fechaEmitida: LocalDate? = null,
    val imagen: String? = "",
    val descripcion: String? = "",
    val monto: BigDecimal = BigDecimal.ZERO,
    val fechaMulta: LocalDate? = null,
    val fechaLimite: LocalDate? = null,
    val pagada: Boolean = false,
    val usuarioId: Long? = null,
    val eliminado: Boolean = false
)
