package ni.edu.uam.autotrak.data.local.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import ni.edu.uam.autotrak.data.remote.model.TipoMantenimiento
import ni.edu.uam.autotrak.data.sync.SyncState
import java.time.LocalDateTime

@Entity(
    tableName = "servicios_mantenimiento",
    indices = [
        Index(value = ["serverId"]),
        Index(value = ["vehiculoId"]),
        Index(value = ["syncState"])
    ]
)
data class ServicioMantenimientoEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0,
    val serverId: Long? = null,
    val fechaCreacion: LocalDateTime? = null,
    val fechaActualizacion: LocalDateTime? = null,
    val activo: Boolean = true,
    val titulo: String = "",
    val descripcion: String? = null,
    val afectaVehiculo: Boolean = false,
    val completado: Boolean = false,
    val distanciaAgendada: Int? = null,
    val observaciones: String? = null,
    val tipoMantenimiento: TipoMantenimiento = TipoMantenimiento.OTRO,
    val vehiculoId: Long? = null,
    val syncState: SyncState = SyncState.SYNCED
)
