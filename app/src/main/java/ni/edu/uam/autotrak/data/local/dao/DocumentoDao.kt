package ni.edu.uam.autotrak.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update

import kotlinx.coroutines.flow.Flow;
import ni.edu.uam.autotrak.data.local.model.DocumentoEntity;

@Dao
interface DocumentoDao {
    @Query("SELECT * FROM documentos ORDER BY fechaEmitida DESC, localId DESC")
    fun observeAll(): Flow<List<DocumentoEntity>>

    @Query("SELECT * FROM documentos WHERE localId = :localId LIMIT 1")
    fun observeByLocalId(localId: Long): Flow<DocumentoEntity?>

    @Query("SELECT * FROM documentos WHERE serverId = :serverId LIMIT 1")
    fun observeByServerId(serverId: Long): Flow<DocumentoEntity?>

    @Query("SELECT * FROM documentos WHERE localId = :localId LIMIT 1")
    suspend fun getByLocalId(localId: Long): DocumentoEntity?

    @Query("SELECT * FROM documentos WHERE serverId = :serverId LIMIT 1")
    suspend fun getByServerId(serverId: Long): DocumentoEntity?

    @Query("SELECT * FROM documentos WHERE syncState != 'SYNCED'")
    suspend fun getPendingSync(): List<DocumentoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(documento: DocumentoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(documento: List<DocumentoEntity>): List<Long>

    @Update
    suspend fun update(documento: DocumentoEntity): Int

    @Delete
    suspend fun delete(documento: DocumentoEntity): Int

    @Query("DELETE FROM documentos")
    suspend fun deleteAll()
}
