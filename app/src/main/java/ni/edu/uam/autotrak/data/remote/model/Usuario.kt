package ni.edu.uam.autotrak.data.remote.model


import ni.edu.uam.autotrak.data.sync.SyncState

data class Usuario(
    val id: Long? = null,
    val nombres: String? = "",
    val apellidos: String? = "",
    val email: String? = "",
    val numeroTel: String? = "",
    val username: String? = "",
    val password: String? = "",
    val pais: String? = "",
    val syncState: SyncState = SyncState.SYNCED,
    val fechaCreacion: java.time.LocalDateTime? = null,
    val fechaActualizacion: java.time.LocalDateTime? = null
)
