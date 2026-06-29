package ni.edu.uam.autotrak.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ni.edu.uam.autotrak.data.local.dao.RegistroCombustibleDao
import ni.edu.uam.autotrak.data.mapper.toRemoteModel
import ni.edu.uam.autotrak.data.mapper.toRoomEntity
import ni.edu.uam.autotrak.data.remote.RetrofitClient
import ni.edu.uam.autotrak.data.remote.model.RegistroCombustible
import ni.edu.uam.autotrak.data.sync.SyncManager
import ni.edu.uam.autotrak.data.sync.SyncState

class RegistroCombustibleRepositoryImpl(
    private val dao: RegistroCombustibleDao,
    private val syncManagerProvider: () -> SyncManager
) : RegistroCombustibleRepository {

    override fun observeByVehiculoId(vehiculoId: Long): Flow<List<RegistroCombustible>> {
        return dao.observeByVehiculoId(vehiculoId).map { entities ->
            entities.map { it.toRemoteModel() }
        }
    }

    override suspend fun refreshByVehiculoId(vehiculoId: Long) {
        val remoteList = RetrofitClient.api_registro_combustible.getRegistroCombustibleByVehiculoId(vehiculoId)
        remoteList.forEach { remote ->
            upsertFromRemote(remote.toRoomEntity(vehiculoId))
        }
    }

    override suspend fun create(vehiculoId: Long, registro: RegistroCombustible): RegistroCombustible {
        val localId = dao.insert(
            registro.toRoomEntity(vehiculoId).copy(syncState = SyncState.PENDING_CREATE)
        )
        return try {
            val remote = RetrofitClient.api_registro_combustible.createRegistroCombustible(vehiculoId, registro)
            persistRemote(remote.toRoomEntity(vehiculoId), localId)
            dao.getByLocalId(localId)?.toRemoteModel() ?: remote
        } catch (_: Exception) {
            dao.getByLocalId(localId)?.toRemoteModel() ?: registro
        }
    }

    override suspend fun getRendimiento(vehiculoId: Long): Double {
        return RetrofitClient.api_registro_combustible.getRendimientoByVehiculoId(vehiculoId)
    }

    override suspend fun getTotalGastado(vehiculoId: Long): Double {
        return RetrofitClient.api_registro_combustible.getTotalGastadoByVehiculoId(vehiculoId)
    }

    private suspend fun upsertFromRemote(entity: ni.edu.uam.autotrak.data.local.model.RegistroCombustibleEntity) {
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

    private suspend fun persistRemote(entity: ni.edu.uam.autotrak.data.local.model.RegistroCombustibleEntity, localId: Long) {
        dao.update(
            entity.copy(
                localId = localId,
                syncState = SyncState.SYNCED
            )
        )
    }
}
