package ni.edu.uam.autotrak.data.local.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import ni.edu.uam.autotrak.data.sync.SyncState

@Entity(
    tableName = "vehiculos",
    indices = [
        Index(value = ["serverId"]),
        Index(value = ["usuarioId"]),
        Index(value = ["syncState"])
    ]
)
data class VehiculoEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0,
    val serverId: Long? = null,
    val marca: String = "",
    val modelo: String = "",
    val anio: Int? = null,
    val placa: String = "",
    val vin: String = "",
    val estado: String = "",
    val apodo: String = "",
    val imagenes: List<String> = emptyList(),
    val usuarioId: Long? = null,
    val syncState: SyncState = SyncState.SYNCED
)
