package ni.edu.uam.autotrak.data.remote.model.sync

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

data class RegistroCombustibleSyncDto(
    val id: Long? = null,
    val fechaRegistro: LocalDate? = null,
    val nota: String? = "",
    val vehiculoId: Long? = null, // Backend might return it this way for sync
    val cantidadCombustible: Double = 0.0,
    val cantidadPagado: BigDecimal? = 0.0.toBigDecimal(),
    val odometro: Long = 0,
    val fechaCreacion: LocalDateTime? = null,
    val fechaActualizacion: LocalDateTime? = null,
    val eliminado: Boolean = false
)
