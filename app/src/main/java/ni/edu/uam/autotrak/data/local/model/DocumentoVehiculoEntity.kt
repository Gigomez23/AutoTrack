package ni.edu.uam.autotrak.data.local.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import ni.edu.uam.autotrak.data.sync.SyncState
import java.time.LocalDate

@Entity(
    tableName = "documentos_vehiculo",
    indices = [
        Index(value = ["serverId"]),
        Index(value = ["vehiculoId"]),
        Index(value = ["syncState"])
    ]
)
data class DocumentoVehiculoEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0,
    val serverId: Long? = null,
    val imagen: String = "",
    val nombre: String = "",
    val vehiculoId: Long? = null,
    val fechaEmitida: LocalDate? = null,
    val fechaVencimiento: LocalDate? = null,
    val syncState: SyncState = SyncState.SYNCED,
    val fechaCreacion: java.time.LocalDateTime? = null,
    val fechaActualizacion: java.time.LocalDateTime? = null
)
