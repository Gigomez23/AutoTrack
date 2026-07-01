package ni.edu.uam.autotrak.data.mapper

import ni.edu.uam.autotrak.data.local.model.LicenciaEntity
import ni.edu.uam.autotrak.data.remote.model.Licencia
import ni.edu.uam.autotrak.data.remote.model.sync.LicenciaSyncDTO
import ni.edu.uam.autotrak.data.sync.SyncState

fun Licencia.toRoomEntity(usuarioId: Long? = null): LicenciaEntity {
    return LicenciaEntity(
        serverId = id,
        imagen = imagen.orEmpty(),
        fechaEmitida = fechaEmitida,
        fechaVencimiento = fechaVencimiento,
        categorias = categorias,
        usuarioId = usuarioId ?: this.usuarioId,
        fechaCreacion = fechaCreacion,
        fechaActualizacion = fechaActualizacion
    )
}

fun LicenciaSyncDTO.toRoomEntity(usuarioId: Long? = null): LicenciaEntity {
    return LicenciaEntity(
        serverId = id,
        imagen = imagen.orEmpty(),
        fechaEmitida = fechaEmitida,
        fechaVencimiento = fechaVencimiento,
        categorias = categorias,
        usuarioId = usuarioId ?: this.usuarioId,
        fechaCreacion = fechaCreacion,
        fechaActualizacion = fechaActualizacion,
        syncState = SyncState.SYNCED
    )
}

fun LicenciaEntity.toRemoteModel(): Licencia {
    return Licencia(
        id = serverId ?: localId.takeIf { it > 0 }?.let { -it },
        fechaCreacion = fechaCreacion,
        fechaActualizacion = fechaActualizacion,
        fechaEmitida = fechaEmitida,
        fechaVencimiento = fechaVencimiento,
        imagen = imagen,
        categorias = categorias,
        usuarioId = usuarioId,
        syncState = syncState
    )
}

fun Licencia.toCreateRequestModel(): Licencia {
    return copy(id = null)
}

fun LicenciaEntity.toSyncDto(eliminado: Boolean = false): LicenciaSyncDTO {
    return LicenciaSyncDTO(
        id = serverId ?: localId.takeIf { it > 0 }?.let { -it },
        fechaCreacion = fechaCreacion,
        fechaActualizacion = fechaActualizacion,
        fechaEmitida = fechaEmitida,
        fechaVencimiento = fechaVencimiento,
        imagen = imagen,
        categorias = categorias,
        usuarioId = usuarioId,
        eliminado = eliminado
    )
}
