package ni.edu.uam.autotrak.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ni.edu.uam.autotrak.data.local.model.RegistroCombustibleEntity

@Dao
interface RegistroCombustibleDao {
    @Query("SELECT * FROM registros_combustible ORDER BY fechaRegistro DESC, localId DESC")
    fun observeAll(): Flow<List<RegistroCombustibleEntity>>

    @Query("SELECT * FROM registros_combustible WHERE localId = :localId LIMIT 1")
    suspend fun getByLocalId(localId: Long): RegistroCombustibleEntity?

    @Query("SELECT * FROM registros_combustible WHERE serverId = :serverId LIMIT 1")
    suspend fun getByServerId(serverId: Long): RegistroCombustibleEntity?

    @Query("SELECT * FROM registros_combustible WHERE vehiculoId = :vehiculoId AND syncState != 'PENDING_DELETE' ORDER BY fechaRegistro DESC, localId DESC")
    fun observeByVehiculoId(vehiculoId: Long): Flow<List<RegistroCombustibleEntity>>

    @Query("SELECT * FROM registros_combustible WHERE syncState != 'SYNCED'")
    suspend fun getPendingSync(): List<RegistroCombustibleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(registro: RegistroCombustibleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(registros: List<RegistroCombustibleEntity>): List<Long>

    @Update
    suspend fun update(registro: RegistroCombustibleEntity): Int

    @Delete
    suspend fun delete(registro: RegistroCombustibleEntity): Int

    @Query("DELETE FROM registros_combustible")
    suspend fun deleteAll()
}
