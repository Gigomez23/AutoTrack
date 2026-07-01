package ni.edu.uam.autotrak.data.repository

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ni.edu.uam.autotrak.data.local.dao.MultaDao
import ni.edu.uam.autotrak.data.local.db.AppDatabase
import ni.edu.uam.autotrak.data.mapper.toCreateRequestModel
import ni.edu.uam.autotrak.data.mapper.toRemoteModel
import ni.edu.uam.autotrak.data.mapper.toRoomEntity
import ni.edu.uam.autotrak.data.remote.RetrofitClient
import ni.edu.uam.autotrak.data.remote.model.Multa
import ni.edu.uam.autotrak.data.sync.SyncConstants
import ni.edu.uam.autotrak.data.sync.SyncManager
import ni.edu.uam.autotrak.data.sync.SyncState

class MultaRepositoryImpl(
    private val database: AppDatabase,
    private val dao: MultaDao,
    private val syncManagerProvider: () -> SyncManager
) : MultaRepository {

    override fun observeByUsuarioId(usuarioId: Long): Flow<List<Multa>> {
        return dao.observeByUsuarioId(usuarioId).map { entities ->
            entities.map { it.toRemoteModel() }
        }
    }

    override suspend fun refreshByUsuarioId(usuarioId: Long) {
        if (usuarioId <= 0) return
        syncManagerProvider().pushMulta()
        val remoteList = RetrofitClient.api_multa.getMultasByUsuarioId(usuarioId)
        database.withTransaction {
            remoteList.forEach { remote ->
                upsertFromRemote(remote.toRoomEntity(usuarioId))
            }
        }
    }

    override suspend fun create(multa: Multa): Multa {
        val localId = dao.insert(
            multa.toRoomEntity().copy(syncState = SyncState.PENDING_CREATE)
        )

        val usuarioId = multa.usuarioId
        if (usuarioId == null || usuarioId <= 0) {
            syncManagerProvider().syncEntity(SyncConstants.ENTITY_MULTA)
            return dao.getByLocalId(localId)?.toRemoteModel() ?: multa
        }

        return try {
            val remote = RetrofitClient.api_multa.createMulta(multa.toCreateRequestModel())
            persistRemote(remote.toRoomEntity(usuarioId), localId, usuarioId)
            dao.getByLocalId(localId)?.toRemoteModel() ?: remote
        } catch (_: Exception) {
            syncManagerProvider().syncEntity(SyncConstants.ENTITY_MULTA)
            dao.getByLocalId(localId)?.toRemoteModel() ?: multa
        }
    }

    override suspend fun update(id: Long, multa: Multa): Multa {
        if (id < 0) {
            val existingLocal = dao.getByLocalId(-id)
            if (existingLocal != null) {
                dao.update(
                    multa.toRoomEntity(existingLocal.usuarioId).copy(
                        localId = existingLocal.localId,
                        serverId = existingLocal.serverId,
                        usuarioId = multa.usuarioId ?: existingLocal.usuarioId,
                        syncState = existingLocal.syncState
                    )
                )
                return dao.getByLocalId(existingLocal.localId)?.toRemoteModel() ?: multa
            }
            return multa
        }

        val existing = dao.getByServerId(id)
        val usuarioId = multa.usuarioId ?: existing?.usuarioId
        val localId = existing?.localId ?: dao.insert(
            multa.toRoomEntity(usuarioId).copy(
                serverId = id,
                usuarioId = usuarioId,
                syncState = SyncState.PENDING_UPDATE
            )
        )

        if (existing != null) {
            dao.update(
                multa.toRoomEntity(usuarioId).copy(
                    localId = localId,
                    serverId = id,
                    usuarioId = usuarioId,
                    syncState = if (existing.syncState == SyncState.PENDING_CREATE) {
                        SyncState.PENDING_CREATE
                    } else {
                        SyncState.PENDING_UPDATE
                    }
                )
            )
        }

        return try {
            val remote = RetrofitClient.api_multa.updateMulta(id, multa.copy(id = id))
            persistRemote(remote.toRoomEntity(usuarioId), localId, usuarioId)
            dao.getByLocalId(localId)?.toRemoteModel() ?: remote
        } catch (_: Exception) {
            syncManagerProvider().syncEntity(SyncConstants.ENTITY_MULTA)
            dao.getByLocalId(localId)?.toRemoteModel() ?: multa
        }
    }

    override suspend fun pagar(id: Long): Multa {
        val existing = if (id < 0) dao.getByLocalId(-id) else dao.getByServerId(id)
        if (existing == null) {
            return Multa(id = id.takeIf { it > 0 })
        }

        val nextState = if (existing.syncState == SyncState.PENDING_CREATE) {
            SyncState.PENDING_CREATE
        } else {
            SyncState.PENDING_UPDATE
        }
        dao.update(existing.copy(pagada = true, syncState = nextState))

        val serverId = existing.serverId
        if (id < 0 || serverId == null) {
            return dao.getByLocalId(existing.localId)?.toRemoteModel() ?: existing.toRemoteModel()
        }

        return try {
            RetrofitClient.api_multa.pagarMulta(serverId)
            dao.update(existing.copy(pagada = true, syncState = SyncState.SYNCED))
            dao.getByLocalId(existing.localId)?.toRemoteModel() ?: existing.copy(pagada = true, syncState = SyncState.SYNCED).toRemoteModel()
        } catch (_: Exception) {
            syncManagerProvider().syncEntity(SyncConstants.ENTITY_MULTA)
            dao.getByLocalId(existing.localId)?.toRemoteModel() ?: existing.copy(pagada = true, syncState = nextState).toRemoteModel()
        }
    }

    override suspend fun delete(id: Long) {
        if (id < 0) {
            dao.getByLocalId(-id)?.let { dao.delete(it) }
            return
        }

        val existing = dao.getByServerId(id)
        if (existing != null) {
            dao.update(existing.copy(syncState = SyncState.PENDING_DELETE))
        }

        try {
            RetrofitClient.api_multa.deleteMulta(id)
            database.withTransaction {
                existing?.let { dao.delete(it) }
            }
        } catch (_: Exception) {
            syncManagerProvider().syncEntity(SyncConstants.ENTITY_MULTA)
        }
    }

    private suspend fun upsertFromRemote(entity: ni.edu.uam.autotrak.data.local.model.MultaEntity) {
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

    private suspend fun persistRemote(entity: ni.edu.uam.autotrak.data.local.model.MultaEntity, localId: Long, usuarioIdFallback: Long?) {
        database.withTransaction {
            dao.update(
                entity.copy(
                    localId = localId,
                    usuarioId = entity.usuarioId ?: usuarioIdFallback,
                    syncState = SyncState.SYNCED
                )
            )
        }
    }
}
