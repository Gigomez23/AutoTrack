package ni.edu.uam.autotrak.data.repository

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ni.edu.uam.autotrak.data.local.dao.RegistroProblemaDao
import ni.edu.uam.autotrak.data.local.db.AppDatabase
import ni.edu.uam.autotrak.data.mapper.toRemoteModel
import ni.edu.uam.autotrak.data.mapper.toRoomEntity
import ni.edu.uam.autotrak.data.remote.RetrofitClient
import ni.edu.uam.autotrak.data.remote.model.RegistroProblema
import ni.edu.uam.autotrak.data.sync.SyncConstants
import ni.edu.uam.autotrak.data.sync.SyncManager
import ni.edu.uam.autotrak.data.sync.SyncState

class RegistroProblemaRepositoryImpl(
    private val database: AppDatabase,
    private val dao: RegistroProblemaDao,
    private val syncManagerProvider: () -> SyncManager
) : RegistroProblemaRepository {

    override fun observeByVehiculoId(vehiculoId: Long): Flow<List<RegistroProblema>> {
        return dao.observeByVehiculoId(vehiculoId).map { entities ->
            entities.map { it.toRemoteModel() }
        }
    }

    override suspend fun refreshByVehiculoId(vehiculoId: Long) {
        if (vehiculoId <= 0) return
        syncManagerProvider().pushProblems()
        val remoteList = RetrofitClient.api_registro_problema.getRegistroProblemaByVehiculoId(vehiculoId)
        database.withTransaction {
            remoteList.forEach { remote ->
                upsertFromRemote(remote.toRoomEntity(vehiculoId))
            }
        }
    }

    override suspend fun create(vehiculoId: Long, registro: RegistroProblema): RegistroProblema {
        val localId = dao.insert(
            registro.toRoomEntity(vehiculoId).copy(syncState = SyncState.PENDING_CREATE)
        )

        if (vehiculoId <= 0) {
            syncManagerProvider().syncEntity(SyncConstants.ENTITY_REGISTRO_PROBLEMA)
            return dao.getByLocalId(localId)?.toRemoteModel() ?: registro
        }

        return try {
            val remote = RetrofitClient.api_registro_problema.createRegistroProblema(vehiculoId, registro)
            persistRemote(remote.toRoomEntity(vehiculoId), localId)
            dao.getByLocalId(localId)?.toRemoteModel() ?: remote
        } catch (_: Exception) {
            syncManagerProvider().syncEntity(SyncConstants.ENTITY_REGISTRO_PROBLEMA)
            dao.getByLocalId(localId)?.toRemoteModel() ?: registro
        }
    }

    override suspend fun update(id: Long, registro: RegistroProblema): RegistroProblema {
        val existing = if (id < 0) dao.getByLocalId(-id) else dao.getByServerId(id)
        val localId = existing?.localId ?: dao.insert(
            registro.toRoomEntity(existing?.vehiculoId).copy(
                serverId = if (id > 0) id else null,
                syncState = SyncState.PENDING_UPDATE
            )
        )

        if (existing != null) {
            dao.update(
                registro.toRoomEntity(existing.vehiculoId).copy(
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

        val parentVehicleId = existing?.vehiculoId ?: dao.getByLocalId(localId)?.vehiculoId
        val serverId = existing?.serverId ?: if (id > 0) id else null
        if (id < 0 || parentVehicleId == null || parentVehicleId <= 0 || serverId == null) {
            return dao.getByLocalId(localId)?.toRemoteModel() ?: registro
        }

        return try {
            val updated = RetrofitClient.api_registro_problema.updateRegistroProblema(
                serverId,
                dao.getByLocalId(localId)?.toRemoteModel() ?: registro
            )
            persistRemote(updated.toRoomEntity(parentVehicleId), localId)
            dao.getByLocalId(localId)?.toRemoteModel() ?: updated
        } catch (_: Exception) {
            syncManagerProvider().syncEntity(SyncConstants.ENTITY_REGISTRO_PROBLEMA)
            dao.getByLocalId(localId)?.toRemoteModel() ?: registro
        }
    }

    override suspend fun delete(id: Long) {
        val existing = if (id < 0) dao.getByLocalId(-id) else dao.getByServerId(id)
        val parentVehicleId = existing?.vehiculoId
        if (existing == null || parentVehicleId == null || parentVehicleId <= 0) {
            existing?.let { dao.delete(it) }
            return
        }

        dao.update(existing.copy(syncState = SyncState.PENDING_DELETE))
        val serverId = existing.serverId
        if (id < 0 || serverId == null) {
            database.withTransaction {
                dao.delete(existing)
            }
            return
        }
        try {
            RetrofitClient.api_registro.deleteRegistro(id)
            database.withTransaction {
                existing.let { dao.delete(it) }
            }
        } catch (_: Exception) {
            syncManagerProvider().syncEntity(SyncConstants.ENTITY_REGISTRO_PROBLEMA)
        }
    }

    private suspend fun upsertFromRemote(entity: ni.edu.uam.autotrak.data.local.model.RegistroProblemaEntity) {
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

    private suspend fun persistRemote(entity: ni.edu.uam.autotrak.data.local.model.RegistroProblemaEntity, localId: Long) {
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
