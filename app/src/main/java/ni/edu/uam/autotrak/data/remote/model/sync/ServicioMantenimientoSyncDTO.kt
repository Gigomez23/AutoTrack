package ni.edu.uam.autotrak.data.remote.model.sync

import ni.edu.uam.autotrak.data.remote.model.TipoMantenimiento
import java.time.LocalDateTime

data class ServicioMantenimientoSyncDTO(
    val id: Long? = null,
    val fechaCreacion: LocalDateTime? = null,
    val fechaActualizacion: LocalDateTime? = null,
    val activo: Boolean = true,
    val titulo: String? = "",
    val descripcion: String? = null,
    val afectaVehiculo: Boolean = false,
    val completado: Boolean = false,
    val distanciaAgendada: Int? = null,
    val observaciones: String? = null,
    val tipoMantenimiento: TipoMantenimiento? = TipoMantenimiento.PREVENTIVO,
    val vehiculoId: Long? = null,
    val eliminado: Boolean = false
)
