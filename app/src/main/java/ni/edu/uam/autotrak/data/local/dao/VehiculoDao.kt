package ni.edu.uam.autotrak.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ni.edu.uam.autotrak.data.local.model.VehiculoEntity

@Dao
interface VehiculoDao {
    @Query("SELECT * FROM vehiculos ORDER BY localId DESC")
    fun observeAll(): Flow<List<VehiculoEntity>>

    @Query("SELECT * FROM vehiculos WHERE localId = :localId LIMIT 1")
    fun observeByLocalId(localId: Long): Flow<VehiculoEntity?>

    @Query("SELECT * FROM vehiculos WHERE serverId = :serverId LIMIT 1")
    fun observeByServerId(serverId: Long): Flow<VehiculoEntity?>

    @Query("SELECT * FROM vehiculos WHERE localId = :localId LIMIT 1")
    suspend fun getByLocalId(localId: Long): VehiculoEntity?

    @Query("SELECT * FROM vehiculos WHERE serverId = :serverId LIMIT 1")
    suspend fun getByServerId(serverId: Long): VehiculoEntity?

    @Query("SELECT * FROM vehiculos WHERE usuarioId = :usuarioId AND syncState != 'PENDING_DELETE' ORDER BY localId DESC")
    fun observeByUsuarioId(usuarioId: Long): Flow<List<VehiculoEntity>>

    @Query("SELECT * FROM vehiculos WHERE syncState != 'SYNCED'")
    suspend fun getPendingSync(): List<VehiculoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vehiculo: VehiculoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vehiculos: List<VehiculoEntity>): List<Long>

    @Update
    suspend fun update(vehiculo: VehiculoEntity): Int

    @Delete
    suspend fun delete(vehiculo: VehiculoEntity): Int

    @Query("DELETE FROM vehiculos")
    suspend fun deleteAll()
}
