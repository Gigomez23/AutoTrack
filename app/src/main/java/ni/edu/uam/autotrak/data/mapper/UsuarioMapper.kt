package ni.edu.uam.autotrak.data.mapper

import ni.edu.uam.autotrak.data.local.model.UsuarioEntity
import ni.edu.uam.autotrak.data.remote.model.Usuario
import ni.edu.uam.autotrak.data.remote.model.sync.UsuarioSyncDto
import ni.edu.uam.autotrak.data.sync.SyncState

fun Usuario.toRoomEntity(): UsuarioEntity {
    return UsuarioEntity(
        serverId = id,
        nombres = nombres.orEmpty(),
        apellidos = apellidos.orEmpty(),
        email = email.orEmpty(),
        numeroTel = numeroTel.orEmpty(),
        username = username.orEmpty(),
        password = password.orEmpty(),
        pais = pais.orEmpty(),
        syncState = syncState,
        fechaCreacion = fechaCreacion,
        fechaActualizacion = fechaActualizacion
    )
}

fun UsuarioSyncDto.toRoomEntity(): UsuarioEntity {
    return UsuarioEntity(
        serverId = id,
        nombres = nombres.orEmpty(),
        apellidos = apellidos.orEmpty(),
        email = email.orEmpty(),
        numeroTel = numeroTel.orEmpty(),
        username = username.orEmpty(),
        pais = pais.orEmpty(),
        fechaCreacion = fechaCreacion,
        fechaActualizacion = fechaActualizacion,
        syncState = SyncState.SYNCED
    )
}

fun UsuarioEntity.toRemoteModel(): Usuario {
    return Usuario(
        id = serverId,
        nombres = nombres,
        apellidos = apellidos,
        email = email,
        numeroTel = numeroTel,
        username = username,
        password = password,
        pais = pais,
        syncState = syncState,
        fechaCreacion = fechaCreacion,
        fechaActualizacion = fechaActualizacion
    )
}
