package ni.edu.uam.autotrak.data.remote.model

import java.math.BigDecimal
import java.time.LocalDate

data class RegistroCombustible(
    override val id: Long? = null,
    override val fechaRegistro: LocalDate? = null,
    override val notas: String = "",
    override val vehiculo: Vehiculo? = null,
    val cantidadCombustible: Double = 0.0,
    val cantidadPagada: BigDecimal = 0.0.toBigDecimal(),
//    val distanciaRecorrida: Double = 0.0,
    val odometro: Long = 0,
) : Registro(id, fechaRegistro, notas, vehiculo)
