package ni.edu.uam.autotrak.data.model

data class RegistroProblema(
    override val id: Long? = null,
    override val fechaRegistro: String? = null,
    override val notas: String = "",
    override val vehiculo: Vehiculo? = null,
    val activo: Boolean = false,
    val afectaVehiculo: Boolean = false,
    val tipoProblema: String = ""
) : Registro(id, fechaRegistro, notas, vehiculo)
