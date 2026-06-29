package ni.edu.uam.autotrak.data.mapper

import ni.edu.uam.autotrak.data.local.model.UsuarioEntity
import ni.edu.uam.autotrak.data.remote.model.Usuario

fun Usuario.toRoomEntity(): UsuarioEntity {
    return UsuarioEntity(
        serverId = id,
        nombres = nombres.orEmpty(),
        apellidos = apellidos.orEmpty(),
        email = email.orEmpty(),
        numeroTel = nueroTel.orEmpty(),
        username = username.orEmpty(),
        password = password.orEmpty(),
        pais = pais.orEmpty()
    )
}

fun UsuarioEntity.toRemoteModel(): Usuario {
    return Usuario(
        id = serverId,
        nombres = nombres,
        apellidos = apellidos,
        email = email,
        nueroTel = numeroTel,
        username = username,
        password = password,
        pais = pais
    )
}
