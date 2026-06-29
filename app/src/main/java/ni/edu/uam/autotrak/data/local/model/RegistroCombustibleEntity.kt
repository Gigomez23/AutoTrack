package ni.edu.uam.autotrak.data.local.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.time.LocalDate
import ni.edu.uam.autotrak.data.sync.SyncState

@Entity(
    tableName = "registros_combustible",
    indices = [
        Index(value = ["serverId"]),
        Index(value = ["vehiculoId"]),
        Index(value = ["syncState"])
    ]
)
data class RegistroCombustibleEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0,
    val serverId: Long? = null,
    val vehiculoId: Long? = null,
    val fechaRegistro: LocalDate? = null,
    val nota: String = "",
    val cantidadCombustible: Double = 0.0,
    val cantidadPagado: BigDecimal = BigDecimal.ZERO,
    val odometro: Long = 0,
    val syncState: SyncState = SyncState.SYNCED,
    val fechaCreacion: java.time.LocalDateTime? = null,
    val fechaActualizacion: java.time.LocalDateTime? = null
)
