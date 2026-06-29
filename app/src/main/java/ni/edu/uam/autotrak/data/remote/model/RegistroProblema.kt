package ni.edu.uam.autotrak.data.remote.model

import java.time.LocalDate

data class RegistroProblema(
    override val id: Long? = null,
    override val fechaRegistro: LocalDate? = null,
    override val nota: String? = "",
//    override val vehiculo: Vehiculo? = null,
    val activo: Boolean = true,
    val afectaVehiculo: Boolean = false,
    val tipoProblema: String? = "",
    val fechaCreacion: java.time.LocalDateTime? = null,
    val fechaActualizacion: java.time.LocalDateTime? = null
) : Registro
