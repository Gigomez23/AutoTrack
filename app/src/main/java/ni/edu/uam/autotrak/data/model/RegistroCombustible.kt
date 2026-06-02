package ni.edu.uam.autotrak.data.model

import java.math.BigDecimal

data class RegistroCombustible(
    override val id: Long? = null,
    override val fechaRegistro: String? = null,
    override val notas: String = "",
    override val vehiculo: Vehiculo? = null,
    val cantidadCombustible: Double = 0.0,
    val costoTotal: BigDecimal = 0.0.toBigDecimal(),
    val distanciaRecorrida: Double = 0.0,
) : Registro(id, fechaRegistro, notas, vehiculo)
