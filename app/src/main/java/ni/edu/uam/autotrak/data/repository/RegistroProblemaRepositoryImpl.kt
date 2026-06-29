package ni.edu.uam.autotrak.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ni.edu.uam.autotrak.data.local.dao.RegistroProblemaDao
import ni.edu.uam.autotrak.data.mapper.toRemoteModel
import ni.edu.uam.autotrak.data.mapper.toRoomEntity
import ni.edu.uam.autotrak.data.remote.api.RegistroProblemaApi
import ni.edu.uam.autotrak.data.remote.model.RegistroProblema

class RegistroProblemaRepositoryImpl(
    private val api: RegistroProblemaApi,
    private val dao: RegistroProblemaDao
) : RegistroProblemaRepository {

    override fun observeByVehiculoId(vehiculoId: Long): Flow<List<RegistroProblema>> {
        return dao.observeByVehiculoId(vehiculoId).map { entities ->
            entities.map { it.toRemoteModel() }
        }
    }

    override suspend fun refreshByVehiculoId(vehiculoId: Long) {
        try {
            val remoteRegistros = api.getRegistroProblemaByVehiculoId(vehiculoId)
            remoteRegistros.forEach { remote ->
                val existing = dao.getByServerId(remote.id ?: -1L)
                if (existing != null) {
                    dao.update(remote.toRoomEntity(vehiculoId).copy(localId = existing.localId))
                } else {
                    dao.insert(remote.toRoomEntity(vehiculoId))
                }
            }
        } catch (e: Exception) {}
    }

    override suspend fun create(vehiculoId: Long, registro: RegistroProblema): RegistroProblema {
        val created = api.createRegistroProblema(vehiculoId, registro)
        dao.insert(created.toRoomEntity(vehiculoId))
        return created
    }

    override suspend fun update(id: Long, registro: RegistroProblema): RegistroProblema {
        val updated = api.updateRegistroProblema(id, registro)
        val existing = dao.getByServerId(id)
        if (existing != null) {
            dao.update(updated.toRoomEntity(existing.vehiculoId).copy(localId = existing.localId))
        } else {
            dao.insert(updated.toRoomEntity())
        }
        return updated
    }

    override suspend fun delete(id: Long) {
        api.deleteRegistroProblema(id)
        val existing = dao.getByServerId(id)
        if (existing != null) {
            dao.delete(existing)
        }
    }
}
