package ni.edu.uam.autotrak.data.repository

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ni.edu.uam.autotrak.data.local.dao.ServicioMantenimientoDao
import ni.edu.uam.autotrak.data.local.db.AppDatabase
import ni.edu.uam.autotrak.data.mapper.toRemoteModel
import ni.edu.uam.autotrak.data.mapper.toRoomEntity
import ni.edu.uam.autotrak.data.remote.RetrofitClient
import ni.edu.uam.autotrak.data.remote.model.ServicioMantenimiento
import ni.edu.uam.autotrak.data.sync.SyncConstants
import ni.edu.uam.autotrak.data.sync.SyncManager
import ni.edu.uam.autotrak.data.sync.SyncState

class ServicioMantenimientoRepositoryImpl(
    private val database: AppDatabase,
    private val dao: ServicioMantenimientoDao,
    private val syncManagerProvider: () -> SyncManager
) : ServicioMantenimientoRepository {

    override fun observeByVehiculoId(vehiculoId: Long): Flow<List<ServicioMantenimiento>> {
        return dao.observeByVehiculoId(vehiculoId).map { entities ->
            entities.map { it.toRemoteModel() }
        }
    }

    override suspend fun refreshByVehiculoId(vehiculoId: Long) {
        if (vehiculoId <= 0) return
        syncManagerProvider().syncEntity(SyncConstants.ENTITY_SERVICIO_MANTENIMIENTO)
        val remoteList = RetrofitClient.api_servicio_mantenimiento.getServiciosByVehiculoId(vehiculoId)
        database.withTransaction {
            remoteList.forEach { remote ->
                upsertFromRemote(remote.toRoomEntity(vehiculoId))
            }
        }
    }

    override suspend fun create(vehiculoId: Long, servicio: ServicioMantenimiento): ServicioMantenimiento {
        val localId = dao.insert(
            servicio.toRoomEntity(vehiculoId).copy(syncState = SyncState.PENDING_CREATE)
        )

        try {
            val remote = RetrofitClient.api_servicio_mantenimiento.createServicioMantenimiento(servicio)
            persistRemote(remote.toRoomEntity(vehiculoId), localId)
            return dao.getByLocalId(localId)?.toRemoteModel() ?: remote
        } catch (e: Exception) {
            syncManagerProvider().syncEntity(SyncConstants.ENTITY_SERVICIO_MANTENIMIENTO)
            return dao.getByLocalId(localId)?.toRemoteModel() ?: servicio
        }
    }

    override suspend fun update(id: Long, servicio: ServicioMantenimiento): ServicioMantenimiento {
        val existing = if (id < 0) dao.getByLocalId(-id) else dao.getByServerId(id)
        val localId = existing?.localId ?: dao.insert(
            servicio.toRoomEntity(existing?.vehiculoId).copy(
                serverId = if (id > 0) id else null,
                syncState = SyncState.PENDING_UPDATE
            )
        )

        if (existing != null) {
            dao.update(
                servicio.toRoomEntity(existing.vehiculoId).copy(
                    localId = localId,
                    serverId = existing.serverId ?: if (id > 0) id else null,
                    syncState = if (existing.syncState == SyncState.PENDING_CREATE) {
                        SyncState.PENDING_CREATE
                    } else {
                        SyncState.PENDING_UPDATE
                    }
                )
            )
        }

        val serverId = existing?.serverId
        if (serverId != null) {
            try {
                val remote = RetrofitClient.api_servicio_mantenimiento.updateServicioMantenimiento(serverId, servicio)
                persistRemote(remote.toRoomEntity(existing.vehiculoId), localId)
            } catch (e: Exception) {
                syncManagerProvider().syncEntity(SyncConstants.ENTITY_SERVICIO_MANTENIMIENTO)
            }
        }
        return dao.getByLocalId(localId)?.toRemoteModel() ?: servicio
    }

    override suspend fun delete(id: Long) {
        val existing = if (id < 0) dao.getByLocalId(-id) else dao.getByServerId(id)
        if (existing == null) return

        dao.update(existing.copy(syncState = SyncState.PENDING_DELETE))
        
        val serverId = existing.serverId
        if (serverId != null) {
            try {
                RetrofitClient.api_servicio_mantenimiento.deleteServicioMantenimiento(serverId)
                database.withTransaction {
                    dao.delete(existing)
                }
            } catch (e: Exception) {
                syncManagerProvider().syncEntity(SyncConstants.ENTITY_SERVICIO_MANTENIMIENTO)
            }
        } else {
            database.withTransaction {
                dao.delete(existing)
            }
        }
    }

    override suspend fun completarServicio(id: Long) {
        val existing = if (id < 0) dao.getByLocalId(-id) else dao.getByServerId(id)
        if (existing == null) return

        dao.update(existing.copy(completado = true, syncState = SyncState.PENDING_UPDATE))

        val serverId = existing.serverId
        if (serverId != null) {
            try {
                RetrofitClient.api_servicio_mantenimiento.completarServicio(serverId)
                val remote = RetrofitClient.api_servicio_mantenimiento.getServicioMantenimientoById(serverId)
                upsertFromRemote(remote.toRoomEntity(existing.vehiculoId))
            } catch (e: Exception) {
                syncManagerProvider().syncEntity(SyncConstants.ENTITY_SERVICIO_MANTENIMIENTO)
            }
        }
    }

    private suspend fun upsertFromRemote(entity: ni.edu.uam.autotrak.data.local.model.ServicioMantenimientoEntity) {
        val existing = entity.serverId?.let { dao.getByServerId(it) }
        val next = entity.copy(
            localId = existing?.localId ?: 0,
            syncState = SyncState.SYNCED
        )
        if (existing == null) {
            dao.insert(next)
        } else {
            dao.update(next)
        }
    }

    private suspend fun persistRemote(entity: ni.edu.uam.autotrak.data.local.model.ServicioMantenimientoEntity, localId: Long) {
        database.withTransaction {
            dao.update(
                entity.copy(
                    localId = localId,
                    syncState = SyncState.SYNCED
                )
            )
        }
    }
}
