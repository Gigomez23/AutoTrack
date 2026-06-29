package ni.edu.uam.autotrak.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ni.edu.uam.autotrak.data.local.model.RegistroEntity

@Dao
interface RegistroDao {
    @Query("SELECT * FROM registros ORDER BY fechaRegistro DESC, localId DESC")
    fun observeAll(): Flow<List<RegistroEntity>>

    @Query("SELECT * FROM registros WHERE localId = :localId LIMIT 1")
    suspend fun getByLocalId(localId: Long): RegistroEntity?

    @Query("SELECT * FROM registros WHERE serverId = :serverId LIMIT 1")
    suspend fun getByServerId(serverId: Long): RegistroEntity?

    @Query("SELECT * FROM registros WHERE vehiculoId = :vehiculoId ORDER BY fechaRegistro DESC, localId DESC")
    fun observeByVehiculoId(vehiculoId: Long): Flow<List<RegistroEntity>>

    @Query("SELECT * FROM registros WHERE syncState != 'SYNCED'")
    suspend fun getPendingSync(): List<RegistroEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(registro: RegistroEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(registros: List<RegistroEntity>): List<Long>

    @Update
    suspend fun update(registro: RegistroEntity): Int

    @Delete
    suspend fun delete(registro: RegistroEntity): Int

    @Query("DELETE FROM registros")
    suspend fun deleteAll()
}
