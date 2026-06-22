package ni.edu.uam.autotrak.data.remote.model

import java.math.BigDecimal
import java.time.LocalDate

data class Reporte(
    val id: Long? = null,
    val costo: BigDecimal = 0.0.toBigDecimal(),
    val fecha: LocalDate? = null,
    val fechaInicio: LocalDate? = null,
    val titulo: String = "",
)
