package ni.edu.uam.autotrak.data.remote.model

import ni.edu.uam.autotrak.data.sync.SyncState

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
    val usuarioId: Long? = null,
    val syncState: SyncState = SyncState.SYNCED,
    val fechaCreacion: java.time.LocalDateTime? = null,
    val fechaActualizacion: java.time.LocalDateTime? = null
)
