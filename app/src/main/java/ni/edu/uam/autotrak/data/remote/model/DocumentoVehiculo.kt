package ni.edu.uam.autotrak.data.remote.model

import java.time.LocalDate

data class DocumentoVehiculo(
    override val id: Long? = null,
    override val fechaEmitida: LocalDate? = null,
    override val fechaVencimiento: LocalDate? = null,
//    override val imagen: String = "",
    val nombre: String? = "",
    val vehiculo: Vehiculo? = null
) : Documento
