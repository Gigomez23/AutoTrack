package ni.edu.uam.autotrak.data.remote.model.sync

import java.time.LocalDate
import java.time.LocalDateTime

data class RegistroProblemaSyncDto(
    val id: Long? = null,
    val fechaRegistro: LocalDate? = null,
    val nota: String? = "",
    val vehiculoId: Long? = null,
    val activo: Boolean = true,
    val afectaVehiculo: Boolean = false,
    val tipoProblema: String? = "",
    val fechaCreacion: LocalDateTime? = null,
    val fechaActualizacion: LocalDateTime? = null,
    val eliminado: Boolean = false
)
