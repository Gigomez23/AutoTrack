package ni.edu.uam.autotrak.data.model

data class Vehiculo(
    val id: Long? = null,
    val modelo: String = "",
    val marca: String = "",
    val anhio: String? = null,
//    val color: String = "",
//    val placa: String = "",
    val vin: String = "",
    val apodo: String = "",
    val estado: String = "",
//    val imagenes: String = "",

    val registros: List<Registro>? = null,

)
