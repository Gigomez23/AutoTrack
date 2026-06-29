package ni.edu.uam.autotrak.data.local.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import ni.edu.uam.autotrak.data.sync.SyncState

@Entity(
    tableName = "usuarios",
    indices = [
        Index(value = ["serverId"]),
        Index(value = ["syncState"])
    ]
)
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0,
    val serverId: Long? = null,
    val nombres: String = "",
    val apellidos: String = "",
    val email: String = "",
    val numeroTel: String = "",
    val username: String = "",
    val password: String = "",
    val pais: String = "",
    val syncState: SyncState = SyncState.SYNCED
)
