package ni.edu.uam.autotrak.data.mapper

import ni.edu.uam.autotrak.data.local.model.RegistroProblemaEntity
import ni.edu.uam.autotrak.data.remote.model.RegistroProblema

fun RegistroProblema.toRoomEntity(vehiculoId: Long? = null): RegistroProblemaEntity {
    return RegistroProblemaEntity(
        serverId = id,
        vehiculoId = vehiculoId,
        fechaRegistro = fechaRegistro,
        nota = nota.orEmpty(),
        activo = activo,
        afectaVehiculo = afectaVehiculo,
        tipoProblema = tipoProblema.orEmpty()
    )
}

fun RegistroProblemaEntity.toRemoteModel(): RegistroProblema {
    return RegistroProblema(
        id = serverId,
        fechaRegistro = fechaRegistro,
        nota = nota,
        activo = activo,
        afectaVehiculo = afectaVehiculo,
        tipoProblema = tipoProblema
    )
}
