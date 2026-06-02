package ni.edu.uam.autotrak.data.model

/**
 * Base class for all types of records associated with a vehicle.
 * Using an open class allows child data classes to inherit common properties.
 */
open class Registro(
    open val id: Long? = null,
    open val fechaRegistro: String? = null,
    open val notas: String = "",
    open val vehiculo: Vehiculo? = null,
)
