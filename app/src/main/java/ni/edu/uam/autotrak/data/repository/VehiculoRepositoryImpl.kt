package ni.edu.uam.autotrak.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ni.edu.uam.autotrak.data.local.dao.VehiculoDao
import ni.edu.uam.autotrak.data.mapper.toRemoteModel
import ni.edu.uam.autotrak.data.mapper.toRoomEntity
import ni.edu.uam.autotrak.data.remote.api.VehiculoApi
import ni.edu.uam.autotrak.data.remote.model.Vehiculo

class VehiculoRepositoryImpl(
    private val vehiculoApi: VehiculoApi,
    private val vehiculoDao: VehiculoDao
) : VehiculoRepository {

    override fun observeVehiculos(usuarioId: Long): Flow<List<Vehiculo>> {
        return vehiculoDao.observeByUsuarioId(usuarioId).map { entities ->
            entities.map { it.toRemoteModel() }
        }
    }

    override suspend fun refreshVehiculos(usuarioId: Long) {
        try {
            val remoteVehiculos = vehiculoApi.getVehiculoByUsuarioId(usuarioId)
            remoteVehiculos.forEach { remote ->
                val existing = vehiculoDao.getByServerId(remote.id ?: -1L)
                val entity = remote.toRoomEntity().let { 
                    if (it.usuarioId == null) it.copy(usuarioId = usuarioId) else it 
                }
                
                if (existing != null) {
                    vehiculoDao.update(entity.copy(localId = existing.localId))
                } else {
                    vehiculoDao.insert(entity)
                }
            }
        } catch (e: Exception) {
            // Behave as current application: do not delete local data
        }
    }

    override fun observeVehiculoById(id: Long): Flow<Vehiculo?> {
        return vehiculoDao.observeAll().map { list ->
            list.find { it.serverId == id }?.toRemoteModel()
        }
    }

    override suspend fun refreshVehiculoById(id: Long) {
        try {
            val remote = vehiculoApi.getVehiculoById(id)
            val existing = vehiculoDao.getByServerId(id)
            if (existing != null) {
                vehiculoDao.update(remote.toRoomEntity().copy(localId = existing.localId))
            } else {
                vehiculoDao.insert(remote.toRoomEntity())
            }
        } catch (e: Exception) {}
    }

    override suspend fun createVehiculo(vehiculo: Vehiculo): Vehiculo {
        val created = vehiculoApi.createVehiculo(vehiculo)
        vehiculoDao.insert(created.toRoomEntity())
        return created
    }

    override suspend fun updateVehiculo(id: Long, vehiculo: Vehiculo): Vehiculo {
        val updated = vehiculoApi.updateVehiculo(id, vehiculo)
        val existing = vehiculoDao.getByServerId(id)
        val entity = updated.toRoomEntity()
        
        if (existing != null) {
            vehiculoDao.update(entity.copy(localId = existing.localId, usuarioId = entity.usuarioId ?: existing.usuarioId))
        } else {
            vehiculoDao.insert(entity)
        }
        return updated
    }

    override suspend fun deleteVehiculo(id: Long) {
        vehiculoApi.deleteVehiculo(id)
        val existing = vehiculoDao.getByServerId(id)
        if (existing != null) {
            vehiculoDao.delete(existing)
        }
    }
}
