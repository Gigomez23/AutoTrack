package ni.edu.uam.autotrak.data.mapper

import ni.edu.uam.autotrak.data.local.model.RegistroCombustibleEntity
import ni.edu.uam.autotrak.data.remote.model.RegistroCombustible
import ni.edu.uam.autotrak.data.remote.model.sync.RegistroCombustibleSyncDto
import ni.edu.uam.autotrak.data.sync.SyncState

fun RegistroCombustible.toRoomEntity(vehiculoId: Long? = null): RegistroCombustibleEntity {
    return RegistroCombustibleEntity(
        serverId = id,
        vehiculoId = vehiculoId,
        fechaRegistro = fechaRegistro,
        nota = nota.orEmpty(),
        cantidadCombustible = cantidadCombustible,
        cantidadPagado = cantidadPagado ?: java.math.BigDecimal.ZERO,
        odometro = odometro,
        fechaCreacion = fechaCreacion,
        fechaActualizacion = fechaActualizacion
    )
}

fun RegistroCombustibleSyncDto.toRoomEntity(vehiculoId: Long? = null): RegistroCombustibleEntity {
    return RegistroCombustibleEntity(
        serverId = id,
        vehiculoId = vehiculoId ?: this.vehiculoId,
        fechaRegistro = fechaRegistro,
        nota = nota.orEmpty(),
        cantidadCombustible = cantidadCombustible,
        cantidadPagado = cantidadPagado ?: java.math.BigDecimal.ZERO,
        odometro = odometro,
        fechaCreacion = fechaCreacion,
        fechaActualizacion = fechaActualizacion,
        syncState = SyncState.SYNCED
    )
}

fun RegistroCombustibleEntity.toRemoteModel(): RegistroCombustible {
    return RegistroCombustible(
        id = serverId ?: localId.takeIf { it > 0 }?.let { -it },
        fechaRegistro = fechaRegistro,
        nota = nota,
        cantidadCombustible = cantidadCombustible,
        cantidadPagado = cantidadPagado,
        odometro = odometro,
        syncState = syncState,
        fechaCreacion = fechaCreacion,
        fechaActualizacion = fechaActualizacion
    )
}
