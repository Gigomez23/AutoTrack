package ni.edu.uam.autotrak.data.mapper

import ni.edu.uam.autotrak.data.local.model.VehiculoEntity
import ni.edu.uam.autotrak.data.remote.model.Usuario
import ni.edu.uam.autotrak.data.remote.model.Vehiculo
import ni.edu.uam.autotrak.data.remote.model.sync.VehiculoSyncDto
import ni.edu.uam.autotrak.data.sync.SyncState

fun Vehiculo.toRoomEntity(): VehiculoEntity {
    return VehiculoEntity(
        serverId = id,
        marca = marca.orEmpty(),
        modelo = modelo.orEmpty(),
        anio = anio,
        placa = placa.orEmpty(),
        vin = vin.orEmpty(),
        estado = estado.orEmpty(),
        apodo = apodo.orEmpty(),
        imagenes = imagenes.orEmpty(),
        usuarioId = usuario?.id,
        fechaCreacion = fechaCreacion,
        fechaActualizacion = fechaActualizacion
    )
}

fun VehiculoSyncDto.toRoomEntity(): VehiculoEntity {
    return VehiculoEntity(
        serverId = id,
        marca = marca.orEmpty(),
        modelo = modelo.orEmpty(),
        anio = anio,
        placa = placa.orEmpty(),
        vin = vin.orEmpty(),
        estado = estado.orEmpty(),
        apodo = apodo.orEmpty(),
        imagenes = imagenes.orEmpty(),
        usuarioId = usuario?.id,
        fechaCreacion = fechaCreacion,
        fechaActualizacion = fechaActualizacion,
        syncState = SyncState.SYNCED
    )
}

fun VehiculoEntity.toRemoteModel(): Vehiculo {
    return Vehiculo(
        id = serverId ?: localId.takeIf { it > 0 }?.let { -it },
        marca = marca,
        modelo = modelo,
        anio = anio,
        placa = placa,
        vin = vin,
        estado = estado,
        apodo = apodo,
        imagenes = imagenes,
        usuario = usuarioId?.let { Usuario(id = it) },
        syncState = syncState,
        fechaCreacion = fechaCreacion,
        fechaActualizacion = fechaActualizacion
    )
}
