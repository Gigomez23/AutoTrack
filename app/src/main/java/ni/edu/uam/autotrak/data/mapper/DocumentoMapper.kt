package ni.edu.uam.autotrak.data.mapper

import ni.edu.uam.autotrak.data.local.model.DocumentoEntity
import ni.edu.uam.autotrak.data.remote.model.Documento
import ni.edu.uam.autotrak.data.remote.model.sync.DocumentoSyncDTO
import ni.edu.uam.autotrak.data.sync.SyncState

fun Documento.toRoomEntity(): DocumentoEntity {
    return DocumentoEntity(
        serverId = id,
        imagen = imagen.orEmpty(),
        fechaEmitida = fechaEmitida,
        fechaVencimiento = fechaVencimiento,
        fechaCreacion = fechaCreacion,
        fechaActualizacion = fechaActualizacion
    )
}

fun DocumentoSyncDTO.toRoomEntity(): DocumentoEntity {
    return DocumentoEntity(
        serverId = id,
        imagen = imagen.orEmpty(),
        fechaEmitida = fechaEmitida,
        fechaVencimiento = fechaVencimiento,
        fechaCreacion = fechaCreacion,
        fechaActualizacion = fechaActualizacion,
        syncState = SyncState.SYNCED
    )
}

fun DocumentoEntity.toRemoteModel(): Documento {
    return Documento(
        id = serverId ?: localId.takeIf { it > 0 }?.let { -it },
        fechaCreacion = fechaCreacion,
        fechaActualizacion = fechaActualizacion,
        fechaVencimiento = fechaVencimiento,
        fechaEmitida = fechaEmitida,
        imagen = imagen,
        syncState = syncState
    )
}

fun DocumentoEntity.toSyncDto(eliminado: Boolean = false): DocumentoSyncDTO {
    return DocumentoSyncDTO(
        id = serverId ?: localId.takeIf { it > 0 }?.let { -it },
        fechaCreacion = fechaCreacion,
        fechaActualizacion = fechaActualizacion,
        fechaVencimiento = fechaVencimiento,
        fechaEmitida = fechaEmitida,
        imagen = imagen,
        eliminado = eliminado
    )
}
