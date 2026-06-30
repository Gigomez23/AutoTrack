package ni.edu.uam.autotrak.data.local.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import ni.edu.uam.autotrak.data.sync.SyncState

@Entity(
    tableName = "registros_problema",
    indices = [
        Index(value = ["serverId"]),
        Index(value = ["vehiculoId"]),
        Index(value = ["syncState"])
    ]
)
data class RegistroProblemaEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0,
    val serverId: Long? = null,
    val vehiculoId: Long? = null,
    val fechaRegistro: LocalDate? = null,
    val nota: String = "",
    val activo: Boolean = true,
    val afectaVehiculo: Boolean = false,
    val tipoProblema: String = "",
    val syncState: SyncState = SyncState.SYNCED,
    val fechaCreacion: java.time.LocalDateTime? = null,
    val fechaActualizacion: java.time.LocalDateTime? = null
)
