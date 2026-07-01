package ni.edu.uam.autotrak.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ni.edu.uam.autotrak.data.local.model.MultaEntity

@Dao
interface MultaDao {
    @Query("SELECT * FROM multas ORDER BY fechaCreacion DESC, localId DESC")
    fun observeAll(): Flow<List<MultaEntity>>

    @Query("SELECT * FROM multas WHERE localId = :localId LIMIT 1")
    fun observeByLocalId(localId: Long): Flow<MultaEntity?>

    @Query("SELECT * FROM multas WHERE serverId = :serverId LIMIT 1")
    fun observeByServerId(serverId: Long): Flow<MultaEntity?>

    @Query("SELECT * FROM multas WHERE localId = :localId LIMIT 1")
    suspend fun getByLocalId(localId: Long): MultaEntity?

    @Query("SELECT * FROM multas WHERE serverId = :serverId LIMIT 1")
    suspend fun getByServerId(serverId: Long): MultaEntity?

    @Query("SELECT * FROM multas WHERE usuarioId = :usuarioId AND syncState != 'PENDING_DELETE' ORDER BY fechaCreacion DESC, localId DESC")
    fun observeByUsuarioId(usuarioId: Long): Flow<List<MultaEntity>>

    @Query("SELECT * FROM multas WHERE syncState != 'SYNCED'")
    suspend fun getPendingSync(): List<MultaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(multa: MultaEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(multas: List<MultaEntity>): List<Long>

    @Update
    suspend fun update(multa: MultaEntity): Int

    @Delete
    suspend fun delete(multa: MultaEntity): Int

    @Query("DELETE FROM multas")
    suspend fun deleteAll()
}
