package ni.edu.uam.autotrak.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ni.edu.uam.autotrak.data.local.model.LicenciaEntity

@Dao
interface LicenciaDao {
    @Query("SELECT * FROM licencias ORDER BY fechaCreacion DESC, localId DESC")
    fun observeAll(): Flow<List<LicenciaEntity>>

    @Query("SELECT * FROM licencias WHERE localId = :localId LIMIT 1")
    fun observeByLocalId(localId: Long): Flow<LicenciaEntity?>

    @Query("SELECT * FROM licencias WHERE serverId = :serverId LIMIT 1")
    fun observeByServerId(serverId: Long): Flow<LicenciaEntity?>

    @Query("SELECT * FROM licencias WHERE localId = :localId LIMIT 1")
    suspend fun getByLocalId(localId: Long): LicenciaEntity?

    @Query("SELECT * FROM licencias WHERE serverId = :serverId LIMIT 1")
    suspend fun getByServerId(serverId: Long): LicenciaEntity?

    @Query("SELECT * FROM licencias WHERE usuarioId = :usuarioId AND syncState != 'PENDING_DELETE' ORDER BY fechaCreacion DESC, localId DESC")
    fun observeByUsuarioId(usuarioId: Long): Flow<List<LicenciaEntity>>

    @Query("UPDATE licencias SET usuarioId = :newUsuarioId WHERE usuarioId = :oldUsuarioId")
    suspend fun updateUsuarioId(oldUsuarioId: Long, newUsuarioId: Long)

    @Query("SELECT * FROM licencias WHERE syncState != 'SYNCED'")
    suspend fun getPendingSync(): List<LicenciaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(licencia: LicenciaEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(licencias: List<LicenciaEntity>): List<Long>

    @Update
    suspend fun update(licencia: LicenciaEntity): Int

    @Delete
    suspend fun delete(licencia: LicenciaEntity): Int

    @Query("DELETE FROM licencias")
    suspend fun deleteAll()
}
