package ni.edu.uam.autotrak.data.mapper

import ni.edu.uam.autotrak.data.local.model.RegistroEntity
import ni.edu.uam.autotrak.data.remote.model.RegistroGeneral

fun RegistroGeneral.toRoomEntity(vehiculoId: Long? = null): RegistroEntity {
    return RegistroEntity(
        serverId = id,
        vehiculoId = vehiculoId,
        fechaRegistro = fechaRegistro,
        nota = nota.orEmpty()
    )
}

fun RegistroEntity.toRemoteModel(): RegistroGeneral {
    return RegistroGeneral(
        id = serverId,
        fechaRegistro = fechaRegistro,
        nota = nota
    )
}
