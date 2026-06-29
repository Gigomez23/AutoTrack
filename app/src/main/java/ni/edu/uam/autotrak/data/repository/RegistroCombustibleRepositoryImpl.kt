package ni.edu.uam.autotrak.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ni.edu.uam.autotrak.data.local.dao.RegistroCombustibleDao
import ni.edu.uam.autotrak.data.mapper.toRemoteModel
import ni.edu.uam.autotrak.data.mapper.toRoomEntity
import ni.edu.uam.autotrak.data.remote.api.RegistroCombustibleApi
import ni.edu.uam.autotrak.data.remote.model.RegistroCombustible

class RegistroCombustibleRepositoryImpl(
    private val api: RegistroCombustibleApi,
    private val dao: RegistroCombustibleDao
) : RegistroCombustibleRepository {

    override fun observeByVehiculoId(vehiculoId: Long): Flow<List<RegistroCombustible>> {
        return dao.observeByVehiculoId(vehiculoId).map { entities ->
            entities.map { it.toRemoteModel() }
        }
    }

    override suspend fun refreshByVehiculoId(vehiculoId: Long) {
        try {
            val remoteRegistros = api.getRegistroCombustibleByVehiculoId(vehiculoId)
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

    override suspend fun create(vehiculoId: Long, registro: RegistroCombustible): RegistroCombustible {
        val created = api.createRegistroCombustible(vehiculoId, registro)
        dao.insert(created.toRoomEntity(vehiculoId))
        return created
    }

    override suspend fun getRendimiento(vehiculoId: Long): Double {
        return try {
            api.getRendimientoByVehiculoId(vehiculoId)
        } catch (e: Exception) {
            0.0
        }
    }

    override suspend fun getTotalGastado(vehiculoId: Long): Double {
        return try {
            api.getTotalGastadoByVehiculoId(vehiculoId)
        } catch (e: Exception) {
            0.0
        }
    }
}
