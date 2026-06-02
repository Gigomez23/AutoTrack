package ni.edu.uam.autotrak.data.model

data class DocumentoVehiculo(
    override val id: Long? = null,
    override val fechaEmitida: String? = null,
    override val fechaVencimiento: String? = null,
//    override val imagen: String = "",
    val nombre: String = "",
    val vehiculo: Vehiculo? = null
) : Documento(id, fechaEmitida, fechaVencimiento)
