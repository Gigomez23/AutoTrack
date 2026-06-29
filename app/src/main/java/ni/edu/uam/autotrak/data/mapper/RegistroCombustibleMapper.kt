package ni.edu.uam.autotrak.data.mapper

import ni.edu.uam.autotrak.data.local.model.RegistroCombustibleEntity
import ni.edu.uam.autotrak.data.remote.model.RegistroCombustible

fun RegistroCombustible.toRoomEntity(vehiculoId: Long? = null): RegistroCombustibleEntity {
    return RegistroCombustibleEntity(
        serverId = id,
        vehiculoId = vehiculoId,
        fechaRegistro = fechaRegistro,
        nota = nota.orEmpty(),
        cantidadCombustible = cantidadCombustible,
        cantidadPagado = cantidadPagado ?: java.math.BigDecimal.ZERO,
        odometro = odometro
    )
}

fun RegistroCombustibleEntity.toRemoteModel(): RegistroCombustible {
    return RegistroCombustible(
        id = serverId,
        fechaRegistro = fechaRegistro,
        nota = nota,
        cantidadCombustible = cantidadCombustible,
        cantidadPagado = cantidadPagado,
        odometro = odometro
    )
}
