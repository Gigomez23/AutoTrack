package ni.edu.uam.autotrak.data.remote.model

import java.time.LocalDate

data class DocumentoVehiculo(
    val id: Long? = null,
    val fechaEmitida: LocalDate? = null,
    val fechaVencimiento: LocalDate? = null,
    val imagen: String? = "",
    val nombre: String? = "",
    val vehiculo: Vehiculo? = null
)
