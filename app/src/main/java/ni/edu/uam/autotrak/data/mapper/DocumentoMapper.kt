package ni.edu.uam.autotrak.data.mapper

import ni.edu.uam.autotrak.data.local.model.DocumentoEntity
import ni.edu.uam.autotrak.data.remote.model.DocumentoGeneral

fun DocumentoGeneral.toRoomEntity(): DocumentoEntity {
    return DocumentoEntity(
        serverId = id,
        fechaEmitida = fechaEmitida,
        fechaVencimiento = fechaVencimiento
    )
}

fun DocumentoEntity.toRemoteModel(): DocumentoGeneral {
    return DocumentoGeneral(
        id = serverId,
        fechaEmitida = fechaEmitida,
        fechaVencimiento = fechaVencimiento
    )
}