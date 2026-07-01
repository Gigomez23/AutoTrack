package ni.edu.uam.autotrak.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ni.edu.uam.autotrak.data.local.model.ServicioMantenimientoEntity

@Dao
interface ServicioMantenimientoDao {
    @Query("SELECT * FROM servicios_mantenimiento ORDER BY fechaCreacion DESC, localId DESC")
    fun observeAll(): Flow<List<ServicioMantenimientoEntity>>

    @Query("SELECT * FROM servicios_mantenimiento WHERE localId = :localId LIMIT 1")
    suspend fun getByLocalId(localId: Long): ServicioMantenimientoEntity?

    @Query("SELECT * FROM servicios_mantenimiento WHERE serverId = :serverId LIMIT 1")
    suspend fun getByServerId(serverId: Long): ServicioMantenimientoEntity?

    @Query("SELECT * FROM servicios_mantenimiento WHERE vehiculoId = :vehiculoId AND syncState != 'PENDING_DELETE' ORDER BY fechaCreacion DESC, localId DESC")
    fun observeByVehiculoId(vehiculoId: Long): Flow<List<ServicioMantenimientoEntity>>

    @Query("UPDATE servicios_mantenimiento SET vehiculoId = :newVehiculoId WHERE vehiculoId = :oldVehiculoId")
    suspend fun updateVehiculoId(oldVehiculoId: Long, newVehiculoId: Long)

    @Query("SELECT * FROM servicios_mantenimiento WHERE syncState != 'SYNCED'")
    suspend fun getPendingSync(): List<ServicioMantenimientoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(servicio: ServicioMantenimientoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(servicios: List<ServicioMantenimientoEntity>): List<Long>

    @Update
    suspend fun update(servicio: ServicioMantenimientoEntity): Int

    @Delete
    suspend fun delete(servicio: ServicioMantenimientoEntity): Int

    @Query("DELETE FROM servicios_mantenimiento")
    suspend fun deleteAll()
}
