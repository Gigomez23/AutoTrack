package ni.edu.uam.autotrak.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ni.edu.uam.autotrak.data.local.dao.RegistroProblemaDao
import ni.edu.uam.autotrak.data.mapper.toRemoteModel
import ni.edu.uam.autotrak.data.mapper.toRoomEntity
import ni.edu.uam.autotrak.data.remote.RetrofitClient
import ni.edu.uam.autotrak.data.remote.model.RegistroProblema
import ni.edu.uam.autotrak.data.sync.SyncManager
import ni.edu.uam.autotrak.data.sync.SyncState

class RegistroProblemaRepositoryImpl(
    private val dao: RegistroProblemaDao,
    private val syncManagerProvider: () -> SyncManager
) : RegistroProblemaRepository {

    override fun observeByVehiculoId(vehiculoId: Long): Flow<List<RegistroProblema>> {
        return dao.observeByVehiculoId(vehiculoId).map { entities ->
            entities.map { it.toRemoteModel() }
        }
    }

    override suspend fun refreshByVehiculoId(vehiculoId: Long) {
        syncManagerProvider().pushProblems()
        val remoteList = RetrofitClient.api_registro_problema.getRegistroProblemaByVehiculoId(vehiculoId)
        remoteList.forEach { remote ->
            upsertFromRemote(remote.toRoomEntity(vehiculoId))
        }
    }

    override suspend fun create(vehiculoId: Long, registro: RegistroProblema): RegistroProblema {
        val localId = dao.insert(
            registro.toRoomEntity(vehiculoId).copy(syncState = SyncState.PENDING_CREATE)
        )
        return try {
            val remote = RetrofitClient.api_registro_problema.createRegistroProblema(vehiculoId, registro)
            persistRemote(remote.toRoomEntity(vehiculoId), localId)
            dao.getByLocalId(localId)?.toRemoteModel() ?: remote
        } catch (_: Exception) {
            syncManagerProvider().syncEntity(ni.edu.uam.autotrak.data.sync.SyncConstants.ENTITY_REGISTRO_PROBLEMA)
            dao.getByLocalId(localId)?.toRemoteModel() ?: registro
        }
    }

    override suspend fun update(id: Long, registro: RegistroProblema): RegistroProblema {
        val existing = dao.getByServerId(id)
        val localId = existing?.localId ?: dao.insert(
            registro.toRoomEntity(existing?.vehiculoId).copy(
                serverId = id,
                syncState = SyncState.PENDING_UPDATE
            )
        )

        if (existing != null) {
            dao.update(
                registro.toRoomEntity(existing.vehiculoId).copy(
                    localId = localId,
                    serverId = id,
                    syncState = if (existing.syncState == SyncState.PENDING_CREATE) SyncState.PENDING_CREATE else SyncState.PENDING_UPDATE
                )
            )
        }

        return try {
            val remote = RetrofitClient.api_registro_problema.updateRegistroProblema(id, registro)
            persistRemote(remote.toRoomEntity(existing?.vehiculoId), localId)
            dao.getByLocalId(localId)?.toRemoteModel() ?: remote
        } catch (_: Exception) {
            syncManagerProvider().syncEntity(ni.edu.uam.autotrak.data.sync.SyncConstants.ENTITY_REGISTRO_PROBLEMA)
            dao.getByLocalId(localId)?.toRemoteModel() ?: registro
        }
    }

    override suspend fun delete(id: Long) {
        val existing = dao.getByServerId(id)
        if (existing != null) {
            dao.update(existing.copy(syncState = SyncState.PENDING_DELETE))
        }
        try {
            RetrofitClient.api_registro_problema.deleteRegistroProblema(id)
            existing?.let { dao.delete(it) }
        } catch (_: Exception) {
            syncManagerProvider().syncEntity(ni.edu.uam.autotrak.data.sync.SyncConstants.ENTITY_REGISTRO_PROBLEMA)
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
        dao.update(
            entity.copy(
                localId = localId,
                syncState = SyncState.SYNCED
            )
        )
    }
}
