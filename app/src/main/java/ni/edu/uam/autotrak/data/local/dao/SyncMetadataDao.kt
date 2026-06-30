package ni.edu.uam.autotrak.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ni.edu.uam.autotrak.data.local.model.SyncMetadataEntity

@Dao
interface SyncMetadataDao {
    @Query("SELECT * FROM sync_metadata WHERE entityName = :entityName LIMIT 1")
    suspend fun getByEntityName(entityName: String): SyncMetadataEntity?

    @Query("SELECT * FROM sync_metadata ORDER BY entityName ASC")
    fun observeAll(): Flow<List<SyncMetadataEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(metadata: SyncMetadataEntity)

    @Query("DELETE FROM sync_metadata WHERE entityName = :entityName")
    suspend fun deleteByEntityName(entityName: String)
}
