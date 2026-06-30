package ni.edu.uam.autotrak.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ni.edu.uam.autotrak.data.local.model.RegistroProblemaEntity

@Dao
interface RegistroProblemaDao {
    @Query("SELECT * FROM registros_problema ORDER BY fechaRegistro DESC, localId DESC")
    fun observeAll(): Flow<List<RegistroProblemaEntity>>

    @Query("SELECT * FROM registros_problema WHERE localId = :localId LIMIT 1")
    suspend fun getByLocalId(localId: Long): RegistroProblemaEntity?

    @Query("SELECT * FROM registros_problema WHERE serverId = :serverId LIMIT 1")
    suspend fun getByServerId(serverId: Long): RegistroProblemaEntity?

    @Query("SELECT * FROM registros_problema WHERE vehiculoId = :vehiculoId AND syncState != 'PENDING_DELETE' ORDER BY fechaRegistro DESC, localId DESC")
    fun observeByVehiculoId(vehiculoId: Long): Flow<List<RegistroProblemaEntity>>

    @Query("UPDATE registros_problema SET vehiculoId = :newVehiculoId WHERE vehiculoId = :oldVehiculoId")
    suspend fun updateVehiculoId(oldVehiculoId: Long, newVehiculoId: Long)

    @Query("SELECT * FROM registros_problema WHERE syncState != 'SYNCED'")
    suspend fun getPendingSync(): List<RegistroProblemaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(registro: RegistroProblemaEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(registros: List<RegistroProblemaEntity>): List<Long>

    @Update
    suspend fun update(registro: RegistroProblemaEntity): Int

    @Delete
    suspend fun delete(registro: RegistroProblemaEntity): Int

    @Query("DELETE FROM registros_problema")
    suspend fun deleteAll()
}
