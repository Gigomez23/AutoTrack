package ni.edu.uam.autotrak.data.remote.model

data class Vehiculo(
    val id: Long? = null,
    val marca: String? = "",
    val modelo: String? = "",
    val anio: Int? = null,
//    val color: String = "",
    val placa: String? = "",
    val vin: String? = "",
    val estado: String? = "",
    val apodo: String? = "",
    val imagenes: List<String>? = emptyList(),
//    val distanciaRecorrida: Long = 0,
    val usuario: Usuario? = null,
//    val vehiculos: List<Vehiculo>? = null,
//    val registros: List<Registro>? = null,
)
