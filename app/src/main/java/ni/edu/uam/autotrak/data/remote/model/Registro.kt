package ni.edu.uam.autotrak.data.remote.model

import java.time.LocalDate

open class Registro(
    open val id: Long? = null,
    open val fechaRegistro: LocalDate? = null,
    open val notas: String = "",
    open val vehiculo: Vehiculo? = null,
)
