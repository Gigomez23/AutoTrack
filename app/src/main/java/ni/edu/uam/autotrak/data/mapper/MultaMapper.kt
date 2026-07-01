package ni.edu.uam.autotrak.data.mapper

import ni.edu.uam.autotrak.data.local.model.MultaEntity
import ni.edu.uam.autotrak.data.remote.model.Multa
import ni.edu.uam.autotrak.data.remote.model.sync.MultaSyncDTO
import ni.edu.uam.autotrak.data.sync.SyncState

fun Multa.toRoomEntity(usuarioId: Long? = null): MultaEntity {
    return MultaEntity(
        serverId = id,
        imagen = imagen.orEmpty(),
        descripcion = descripcion.orEmpty(),
        monto = monto,
        fechaMulta = fechaMulta,
        fechaLimite = fechaLimite,
        fechaEmitida = fechaEmitida,
        fechaVencimiento = fechaVencimiento,
        pagada = pagada,
        usuarioId = usuarioId ?: this.usuarioId,
        fechaCreacion = fechaCreacion,
        fechaActualizacion = fechaActualizacion
    )
}

fun MultaSyncDTO.toRoomEntity(usuarioId: Long? = null): MultaEntity {
    return MultaEntity(
        serverId = id,
        imagen = imagen.orEmpty(),
        descripcion = descripcion.orEmpty(),
        monto = monto,
        fechaMulta = fechaMulta,
        fechaLimite = fechaLimite,
        fechaEmitida = fechaEmitida,
        fechaVencimiento = fechaVencimiento,
        pagada = pagada,
        usuarioId = usuarioId ?: this.usuarioId,
        fechaCreacion = fechaCreacion,
        fechaActualizacion = fechaActualizacion,
        syncState = SyncState.SYNCED
    )
}

fun MultaEntity.toRemoteModel(): Multa {
    return Multa(
        id = serverId ?: localId.takeIf { it > 0 }?.let { -it },
        fechaCreacion = fechaCreacion,
        fechaActualizacion = fechaActualizacion,
        fechaVencimiento = fechaVencimiento,
        fechaEmitida = fechaEmitida,
        imagen = imagen,
        descripcion = descripcion,
        monto = monto,
        fechaMulta = fechaMulta,
        fechaLimite = fechaLimite,
        pagada = pagada,
        usuarioId = usuarioId,
        syncState = syncState
    )
}

fun Multa.toCreateRequestModel(): Multa {
    return copy(id = null)
}

fun MultaEntity.toSyncDto(eliminado: Boolean = false): MultaSyncDTO {
    return MultaSyncDTO(
        id = serverId ?: localId.takeIf { it > 0 }?.let { -it },
        fechaCreacion = fechaCreacion,
        fechaActualizacion = fechaActualizacion,
        fechaVencimiento = fechaVencimiento,
        fechaEmitida = fechaEmitida,
        imagen = imagen,
        descripcion = descripcion,
        monto = monto,
        fechaMulta = fechaMulta,
        fechaLimite = fechaLimite,
        pagada = pagada,
        usuarioId = usuarioId,
        eliminado = eliminado
    )
}
