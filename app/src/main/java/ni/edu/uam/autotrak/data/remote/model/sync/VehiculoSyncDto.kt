package ni.edu.uam.autotrak.data.remote.model.sync

import ni.edu.uam.autotrak.data.remote.model.Usuario
import java.time.LocalDateTime

data class VehiculoSyncDto(
    val id: Long? = null,
    val marca: String? = "",
    val modelo: String? = "",
    val anio: Int? = null,
    val placa: String? = "",
    val vin: String? = "",
    val estado: String? = "",
    val apodo: String? = "",
    val imagenes: List<String>? = emptyList(),
    val usuario: Usuario? = null,
    val fechaCreacion: LocalDateTime? = null,
    val fechaActualizacion: LocalDateTime? = null,
    val eliminado: Boolean = false
)
