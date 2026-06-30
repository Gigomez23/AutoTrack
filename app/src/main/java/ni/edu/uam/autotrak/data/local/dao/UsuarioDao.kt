package ni.edu.uam.autotrak.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ni.edu.uam.autotrak.data.local.model.UsuarioEntity

@Dao
interface UsuarioDao {
    @Query("SELECT * FROM usuarios ORDER BY localId DESC")
    fun observeAll(): Flow<List<UsuarioEntity>>

    @Query("SELECT * FROM usuarios WHERE localId = :localId LIMIT 1")
    fun observeByLocalId(localId: Long): Flow<UsuarioEntity?>

    @Query("SELECT * FROM usuarios WHERE serverId = :serverId LIMIT 1")
    fun observeByServerId(serverId: Long): Flow<UsuarioEntity?>

    @Query("SELECT * FROM usuarios WHERE localId = :localId LIMIT 1")
    suspend fun getByLocalId(localId: Long): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE serverId = :serverId LIMIT 1")
    suspend fun getByServerId(serverId: Long): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE syncState != 'SYNCED'")
    suspend fun getPendingSync(): List<UsuarioEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(usuario: UsuarioEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(usuarios: List<UsuarioEntity>): List<Long>

    @Update
    suspend fun update(usuario: UsuarioEntity): Int

    @Delete
    suspend fun delete(usuario: UsuarioEntity): Int

    @Query("DELETE FROM usuarios")
    suspend fun deleteAll()
}
