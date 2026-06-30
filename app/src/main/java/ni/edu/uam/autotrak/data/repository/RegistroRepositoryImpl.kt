package ni.edu.uam.autotrak.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ni.edu.uam.autotrak.data.local.dao.RegistroDao
import ni.edu.uam.autotrak.data.mapper.toRemoteModel
import ni.edu.uam.autotrak.data.mapper.toRoomEntity
import ni.edu.uam.autotrak.data.remote.model.RegistroGeneral
import ni.edu.uam.autotrak.data.remote.RetrofitClient
import ni.edu.uam.autotrak.data.sync.SyncManager
import ni.edu.uam.autotrak.data.sync.SyncState

class RegistroRepositoryImpl(
    private val dao: RegistroDao,
    private val syncManagerProvider: () -> SyncManager
) : RegistroRepository {

    override fun observeAll(): Flow<List<RegistroGeneral>> {
        return dao.observeAll().map { entities ->
            entities.map { it.toRemoteModel() }
        }
    }

    override suspend fun refreshAll() {
        RetrofitClient.api_registro.getRegistros().forEach { remote ->
            upsertFromRemote(remote.toRoomEntity())
        }
    }

    override fun observeByVehiculoId(vehiculoId: Long): Flow<List<RegistroGeneral>> {
        return dao.observeByVehiculoId(vehiculoId).map { entities ->
            entities.map { it.toRemoteModel() }
        }
    }

    override suspend fun refreshByVehiculoId(vehiculoId: Long) {
        RetrofitClient.api_registro.getRegistrosByVehiculoId(vehiculoId).forEach { remote ->
            upsertFromRemote(remote.toRoomEntity(vehiculoId))
        }
    }

    override suspend fun delete(id: Long) {
        val existing = dao.getByServerId(id)
        if (existing != null) {
            dao.update(existing.copy(syncState = SyncState.PENDING_DELETE))
        }
        try {
            RetrofitClient.api_registro.deleteRegistro(id)
            existing?.let { dao.delete(it) }
        } catch (_: Exception) {
            existing?.let { dao.update(it.copy(syncState = SyncState.PENDING_DELETE)) }
        }
    }

    private suspend fun upsertFromRemote(entity: ni.edu.uam.autotrak.data.local.model.RegistroEntity) {
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
