package ni.edu.uam.autotrak.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ni.edu.uam.autotrak.data.local.dao.RegistroDao
import ni.edu.uam.autotrak.data.mapper.toRemoteModel
import ni.edu.uam.autotrak.data.mapper.toRoomEntity
import ni.edu.uam.autotrak.data.remote.api.RegistroApi
import ni.edu.uam.autotrak.data.remote.model.RegistroGeneral

class RegistroRepositoryImpl(
    private val api: RegistroApi,
    private val dao: RegistroDao
) : RegistroRepository {

    override fun observeAll(): Flow<List<RegistroGeneral>> {
        return dao.observeAll().map { entities ->
            entities.map { it.toRemoteModel() }
        }
    }

    override suspend fun refreshAll() {
        try {
            val remote = api.getRegistros()
            remote.forEach { r ->
                val existing = dao.getByServerId(r.id ?: -1L)
                if (existing != null) {
                    dao.update(r.toRoomEntity(existing.vehiculoId).copy(localId = existing.localId))
                } else {
                    dao.insert(r.toRoomEntity())
                }
            }
        } catch (e: Exception) {}
    }

    override fun observeByVehiculoId(vehiculoId: Long): Flow<List<RegistroGeneral>> {
        return dao.observeByVehiculoId(vehiculoId).map { entities ->
            entities.map { it.toRemoteModel() }
        }
    }

    override suspend fun refreshByVehiculoId(vehiculoId: Long) {
        try {
            val remote = api.getRegistrosByVehiculoId(vehiculoId)
            remote.forEach { r ->
                val existing = dao.getByServerId(r.id ?: -1L)
                if (existing != null) {
                    dao.update(r.toRoomEntity(vehiculoId).copy(localId = existing.localId))
                } else {
                    dao.insert(r.toRoomEntity(vehiculoId))
                }
            }
        } catch (e: Exception) {}
    }

    override suspend fun delete(id: Long) {
        api.deleteRegistro(id)
        val existing = dao.getByServerId(id)
        if (existing != null) {
            dao.delete(existing)
        }
    }
}
