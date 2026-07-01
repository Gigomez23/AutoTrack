package ni.edu.uam.autotrak.data.mapper

import ni.edu.uam.autotrak.data.local.model.DocumentoVehiculoEntity
import ni.edu.uam.autotrak.data.remote.model.DocumentoVehiculo
import ni.edu.uam.autotrak.data.remote.model.sync.DocumentoVehiculoSyncDTO
import ni.edu.uam.autotrak.data.sync.SyncState

fun DocumentoVehiculo.toRoomEntity(vehiculoId: Long? = null): DocumentoVehiculoEntity {
    return DocumentoVehiculoEntity(
        serverId = id,
        imagen = imagen.orEmpty(),
        nombre = nombre.orEmpty(),
        vehiculoId = vehiculoId ?: this.vehiculoId,
        fechaEmitida = fechaEmitida,
        fechaVencimiento = fechaVencimiento,
        fechaCreacion = fechaCreacion,
        fechaActualizacion = fechaActualizacion
    )
}

fun DocumentoVehiculoSyncDTO.toRoomEntity(vehiculoId: Long? = null): DocumentoVehiculoEntity {
    return DocumentoVehiculoEntity(
        serverId = id,
        imagen = imagen.orEmpty(),
        nombre = nombre.orEmpty(),
        vehiculoId = vehiculoId ?: this.vehiculoId,
        fechaEmitida = fechaEmitida,
        fechaVencimiento = fechaVencimiento,
        fechaCreacion = fechaCreacion,
        fechaActualizacion = fechaActualizacion,
        syncState = SyncState.SYNCED
    )
}

fun DocumentoVehiculoEntity.toRemoteModel(): DocumentoVehiculo {
    return DocumentoVehiculo(
        id = serverId ?: localId.takeIf { it > 0 }?.let { -it },
        fechaCreacion = fechaCreacion,
        fechaActualizacion = fechaActualizacion,
        fechaVencimiento = fechaVencimiento,
        fechaEmitida = fechaEmitida,
        imagen = imagen,
        nombre = nombre,
        vehiculoId = vehiculoId,
        syncState = syncState
    )
}

fun DocumentoVehiculo.toCreateRequestModel(): DocumentoVehiculo {
    return copy(id = null)
}

fun DocumentoVehiculoEntity.toSyncDto(eliminado: Boolean = false): DocumentoVehiculoSyncDTO {
    return DocumentoVehiculoSyncDTO(
        id = serverId ?: localId.takeIf { it > 0 }?.let { -it },
        fechaCreacion = fechaCreacion,
        fechaActualizacion = fechaActualizacion,
        fechaVencimiento = fechaVencimiento,
        fechaEmitida = fechaEmitida,
        imagen = imagen,
        nombre = nombre,
        vehiculoId = vehiculoId,
        eliminado = eliminado
    )
}
