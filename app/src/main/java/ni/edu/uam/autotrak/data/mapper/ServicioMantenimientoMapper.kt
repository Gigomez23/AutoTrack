package ni.edu.uam.autotrak.data.mapper

import ni.edu.uam.autotrak.data.local.model.ServicioMantenimientoEntity
import ni.edu.uam.autotrak.data.remote.model.ServicioMantenimiento
import ni.edu.uam.autotrak.data.remote.model.TipoMantenimiento
import ni.edu.uam.autotrak.data.remote.model.sync.ServicioMantenimientoSyncDTO
import ni.edu.uam.autotrak.data.sync.SyncState

fun ServicioMantenimiento.toRoomEntity(vehiculoId: Long? = null): ServicioMantenimientoEntity {
    return ServicioMantenimientoEntity(
        serverId = id,
        fechaCreacion = fechaCreacion,
        fechaActualizacion = fechaActualizacion,
        activo = activo,
        titulo = titulo ?: "",
        descripcion = descripcion,
        afectaVehiculo = afectaVehiculo,
        completado = completado,
        distanciaAgendada = distanciaAgendada,
        observaciones = observaciones,
        tipoMantenimiento = tipoMantenimiento ?: TipoMantenimiento.PREVENTIVO,
        vehiculoId = vehiculoId ?: this.vehiculoId,
        syncState = SyncState.SYNCED
    )
}

fun ServicioMantenimientoSyncDTO.toRoomEntity(vehiculoId: Long? = null): ServicioMantenimientoEntity {
    return ServicioMantenimientoEntity(
        serverId = id,
        fechaCreacion = fechaCreacion,
        fechaActualizacion = fechaActualizacion,
        activo = activo,
        titulo = titulo ?: "",
        descripcion = descripcion,
        afectaVehiculo = afectaVehiculo,
        completado = completado,
        distanciaAgendada = distanciaAgendada,
        observaciones = observaciones,
        tipoMantenimiento = tipoMantenimiento ?: TipoMantenimiento.PREVENTIVO,
        vehiculoId = vehiculoId ?: this.vehiculoId,
        syncState = SyncState.SYNCED
    )
}

fun ServicioMantenimientoEntity.toRemoteModel(): ServicioMantenimiento {
    return ServicioMantenimiento(
        id = serverId ?: localId.takeIf { it > 0 }?.let { -it },
        fechaCreacion = fechaCreacion,
        fechaActualizacion = fechaActualizacion,
        activo = activo,
        titulo = titulo,
        descripcion = descripcion,
        afectaVehiculo = afectaVehiculo,
        completado = completado,
        distanciaAgendada = distanciaAgendada,
        observaciones = observaciones,
        tipoMantenimiento = tipoMantenimiento,
        vehiculoId = vehiculoId,
        syncState = syncState
    )
}

fun ServicioMantenimientoEntity.toSyncDto(eliminado: Boolean = false): ServicioMantenimientoSyncDTO {
    return ServicioMantenimientoSyncDTO(
        id = serverId,
        fechaCreacion = fechaCreacion,
        fechaActualizacion = fechaActualizacion,
        activo = activo,
        titulo = titulo,
        descripcion = descripcion,
        afectaVehiculo = afectaVehiculo,
        completado = completado,
        distanciaAgendada = distanciaAgendada,
        observaciones = observaciones,
        tipoMantenimiento = tipoMantenimiento,
        vehiculoId = vehiculoId,
        eliminado = eliminado
    )
}
