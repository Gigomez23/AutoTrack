package ni.edu.uam.autotrak.data.local.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import ni.edu.uam.autotrak.data.sync.SyncState
import java.math.BigDecimal
import java.time.LocalDate

@Entity(
    tableName = "multas",
    indices = [
        Index(value = ["serverId"]),
        Index(value = ["usuarioId"]),
        Index(value = ["syncState"])
    ]
)
data class MultaEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0,
    val serverId: Long? = null,
    val imagen: String = "",
    val descripcion: String = "",
    val monto: BigDecimal = BigDecimal.ZERO,
    val fechaMulta: LocalDate? = null,
    val fechaLimite: LocalDate? = null,
    val fechaEmitida: LocalDate? = null,
    val fechaVencimiento: LocalDate? = null,
    val pagada: Boolean = false,
    val usuarioId: Long? = null,
    val syncState: SyncState = SyncState.SYNCED,
    val fechaCreacion: java.time.LocalDateTime? = null,
    val fechaActualizacion: java.time.LocalDateTime? = null
)
