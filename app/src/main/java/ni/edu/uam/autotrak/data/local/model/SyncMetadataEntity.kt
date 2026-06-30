package ni.edu.uam.autotrak.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_metadata")
data class SyncMetadataEntity(
    @PrimaryKey
    val entityName: String,
    val lastSuccessfulSyncServerTimeMillis: Long? = null
)
