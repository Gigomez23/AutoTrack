package ni.edu.uam.autotrak.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ni.edu.uam.autotrak.data.local.model.DocumentoVehiculoEntity

@Dao
interface DocumentoVehiculoDao {
    @Query("SELECT * FROM documentos_vehiculo ORDER BY fechaCreacion DESC, localId DESC")
    fun observeAll(): Flow<List<DocumentoVehiculoEntity>>

    @Query("SELECT * FROM documentos_vehiculo WHERE localId = :localId LIMIT 1")
    fun observeByLocalId(localId: Long): Flow<DocumentoVehiculoEntity?>

    @Query("SELECT * FROM documentos_vehiculo WHERE serverId = :serverId LIMIT 1")
    fun observeByServerId(serverId: Long): Flow<DocumentoVehiculoEntity?>

    @Query("SELECT * FROM documentos_vehiculo WHERE localId = :localId LIMIT 1")
    suspend fun getByLocalId(localId: Long): DocumentoVehiculoEntity?

    @Query("SELECT * FROM documentos_vehiculo WHERE serverId = :serverId LIMIT 1")
    suspend fun getByServerId(serverId: Long): DocumentoVehiculoEntity?

    @Query("SELECT * FROM documentos_vehiculo WHERE vehiculoId = :vehiculoId AND syncState != 'PENDING_DELETE' ORDER BY fechaCreacion DESC, localId DESC")
    fun observeByVehiculoId(vehiculoId: Long): Flow<List<DocumentoVehiculoEntity>>

    @Query("SELECT * FROM documentos_vehiculo WHERE syncState != 'SYNCED'")
    suspend fun getPendingSync(): List<DocumentoVehiculoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(documentoVehiculo: DocumentoVehiculoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(documentoVehiculos: List<DocumentoVehiculoEntity>): List<Long>

    @Update
    suspend fun update(documentoVehiculo: DocumentoVehiculoEntity): Int

    @Delete
    suspend fun delete(documentoVehiculo: DocumentoVehiculoEntity): Int

    @Query("DELETE FROM documentos_vehiculo")
    suspend fun deleteAll()
}
