package ni.edu.uam.autotrak.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ni.edu.uam.autotrak.data.local.dao.DocumentoDao
import ni.edu.uam.autotrak.data.mapper.toRemoteModel
import ni.edu.uam.autotrak.data.mapper.toRoomEntity
import ni.edu.uam.autotrak.data.mapper.toSyncDto
import ni.edu.uam.autotrak.data.remote.RetrofitClient
import ni.edu.uam.autotrak.data.remote.model.Documento
import ni.edu.uam.autotrak.data.sync.SyncConstants
import ni.edu.uam.autotrak.data.sync.SyncManager
import ni.edu.uam.autotrak.data.sync.SyncState

class DocumentoRepositoryImpl(
    private val dao: DocumentoDao,
    private val syncManagerProvider: () -> SyncManager
) : DocumentoRepository {
    override fun observeAll(): Flow<List<Documento>> {
        return dao.observeAll().map { entities ->
            entities.map { it.toRemoteModel() }
        }
    }

    override suspend fun refreshAll() {
        syncManagerProvider().syncEntity(SyncConstants.ENTITY_DOCUMENTO)
    }

    override suspend fun create(documento: Documento): Documento {
        val localId = dao.insert(
            documento.toRoomEntity().copy(syncState = SyncState.PENDING_CREATE)
        )

        return try {
            val remote = RetrofitClient.api_documento.updateDocumento(
                documento.copy(id = null)
            )
            persistRemote(remote, localId)
            dao.getByLocalId(localId)?.toRemoteModel() ?: remote
        } catch (_: Exception) {
            syncManagerProvider().syncEntity(SyncConstants.ENTITY_DOCUMENTO)
            dao.getByLocalId(localId)?.toRemoteModel() ?: documento
        }
    }

    override suspend fun update(id: Long, documento: Documento): Documento {
        if (id < 0) {
            val existingLocal = dao.getByLocalId(-id)
            if (existingLocal != null) {
                dao.update(
                    documento.toRoomEntity().copy(
                        localId = existingLocal.localId,
                        serverId = existingLocal.serverId,
                        syncState = existingLocal.syncState
                    )
                )
                return dao.getByLocalId(existingLocal.localId)?.toRemoteModel() ?: documento
            }
            return documento
        }

        val existing = dao.getByServerId(id)
        val localId = existing?.localId ?: dao.insert(
            documento.toRoomEntity().copy(
                serverId = id,
                syncState = SyncState.PENDING_UPDATE
            )
        )

        if (existing != null) {
            dao.update(
                documento.toRoomEntity().copy(
                    localId = localId,
                    serverId = id,
                    syncState = if (existing.syncState == SyncState.PENDING_CREATE) {
                        SyncState.PENDING_CREATE
                    } else {
                        SyncState.PENDING_UPDATE
                    }
                )
            )
        }

        return try {
            val remote = RetrofitClient.api_documento.updateDocumento(
                documento.copy(id = id)
            )
            persistRemote(remote, localId)
            dao.getByLocalId(localId)?.toRemoteModel() ?: remote
        } catch (_: Exception) {
            syncManagerProvider().syncEntity(SyncConstants.ENTITY_DOCUMENTO)
            dao.getByLocalId(localId)?.toRemoteModel() ?: documento
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
            existing?.let {
                RetrofitClient.api_documento.deleteDocumento(it.toSyncDto(eliminado = true))
            }
            existing?.let { dao.delete(it) }
        } catch (_: Exception) {
            syncManagerProvider().syncEntity(SyncConstants.ENTITY_DOCUMENTO)
        }
    }

    private suspend fun persistRemote(remote: Documento, localId: Long) {
        dao.update(
            remote.toRoomEntity().copy(
                localId = localId,
                syncState = SyncState.SYNCED
            )
        )
    }
}
