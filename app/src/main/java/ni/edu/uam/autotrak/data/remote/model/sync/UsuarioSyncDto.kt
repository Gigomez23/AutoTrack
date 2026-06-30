package ni.edu.uam.autotrak.data.remote.model.sync

import java.time.LocalDateTime

data class UsuarioSyncDto(
    val id: Long? = null,
    val nombres: String? = "",
    val apellidos: String? = "",
    val email: String? = "",
    val numeroTel: String? = "",
    val username: String? = "",
    val pais: String? = "",
    val fechaCreacion: LocalDateTime? = null,
    val fechaActualizacion: LocalDateTime? = null,
    val eliminado: Boolean = false
)
