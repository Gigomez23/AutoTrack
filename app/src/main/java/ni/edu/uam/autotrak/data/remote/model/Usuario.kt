package ni.edu.uam.autotrak.data.remote.model


data class Usuario(
    val id: Long? = null,
    val nombres: String? = "",
    val apellidos: String? = "",
    val email: String? = "",
    val nueroTel: String? = "",
    val username: String? = "",
    val password: String? = "",
    val pais: String? = "",
    val fechaCreacion: java.time.LocalDateTime? = null,
    val fechaActualizacion: java.time.LocalDateTime? = null
)
