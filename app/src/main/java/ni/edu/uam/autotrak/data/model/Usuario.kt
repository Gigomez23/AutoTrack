package ni.edu.uam.autotrak.data.model

data class Usuario(
    val id: Long? = null,
    val apellidos: String = "",
    val correo: String = "",
    val nombres: String = "",
    val nueroTel: String = "",
    val pais: String = "",
    val username: String = "",
//    todo: implement this shi
    val password: String = ""
)
