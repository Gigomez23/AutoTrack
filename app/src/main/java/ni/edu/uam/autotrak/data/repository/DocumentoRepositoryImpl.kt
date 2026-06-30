package ni.edu.uam.autotrak.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ni.edu.uam.autotrak.data.local.dao.DocumentoDao
import ni.edu.uam.autotrak.data.mapper.toRemoteModel
import ni.edu.uam.autotrak.data.mapper.toRoomEntity
import ni.edu.uam.autotrak.data.remote.RetrofitClient
import ni.edu.uam.autotrak.data.remote.model.DocumentoGeneral
import ni.edu.uam.autotrak.data.sync.SyncManager
import ni.edu.uam.autotrak.data.sync.SyncState

class DocumentoRepositoryImpl(
    private val dao: DocumentoDao,
    private val syncManagerProvider: () -> SyncManager
) : DocumentoRepository{
    override fun observeAll(): Flow<List<DocumentoGeneral>> {
        return dao.observeAll().map { entities ->
            entities.map { it.toRemoteModel() }
        }
    }

    override suspend fun refreshAll() {
        RetrofitClient.api_documento.getDocumentos().forEach { remote ->
            upsertFromRemote(remote.toRoomEntity())
        }
    }

    override suspend fun delete(id: Long) {
        val existing = dao.getByServerId(id)
        if (existing != null) {
            dao.update(existing.copy(syncState = SyncState.PENDING_DELETE))
        }
        try {
            RetrofitClient.api_documento.deleteDocumento(id)
            existing?.let { dao.delete(it) }
        } catch (_: Exception) {
            existing?.let {dao.update(it.copy(syncState = SyncState.PENDING_DELETE))}
        }
    }

    private suspend fun upsertFromRemote(entity: ni.edu.uam.autotrak.data.local.model.DocumentoEntity) {
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

}