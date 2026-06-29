package ni.edu.uam.autotrak.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ni.edu.uam.autotrak.data.local.dao.VehiculoDao
import ni.edu.uam.autotrak.data.mapper.toRemoteModel
import ni.edu.uam.autotrak.data.mapper.toRoomEntity
import ni.edu.uam.autotrak.data.remote.RetrofitClient
import ni.edu.uam.autotrak.data.remote.model.Vehiculo
import ni.edu.uam.autotrak.data.sync.SyncManager
import ni.edu.uam.autotrak.data.sync.SyncState

class VehiculoRepositoryImpl(
    private val vehiculoDao: VehiculoDao,
    private val syncManagerProvider: () -> SyncManager
) : VehiculoRepository {

    override fun observeVehiculos(usuarioId: Long): Flow<List<Vehiculo>> {
        return vehiculoDao.observeByUsuarioId(usuarioId).map { entities ->
            entities.map { it.toRemoteModel() }
        }
    }

    override suspend fun refreshVehiculos(usuarioId: Long) {
        syncManagerProvider().pushVehiculo()
        val remoteList = RetrofitClient.api_vehiculo.getVehiculoByUsuarioId(usuarioId)
        remoteList.forEach { remote ->
            upsertFromRemote(remote, usuarioId)
        }
    }

    override fun observeVehiculoById(id: Long): Flow<Vehiculo?> {
        return vehiculoDao.observeAll().map { list ->
            list.find { it.serverId == id }?.toRemoteModel()
        }
    }

    override suspend fun refreshVehiculoById(id: Long) {
        syncManagerProvider().pushVehiculo()
        val remote = RetrofitClient.api_vehiculo.getVehiculoById(id)
        upsertFromRemote(remote, remote.usuario?.id)
    }

    override suspend fun createVehiculo(vehiculo: Vehiculo): Vehiculo {
        val localId = vehiculoDao.insert(
            vehiculo.toRoomEntity().copy(syncState = SyncState.PENDING_CREATE)
        )
        return try {
            val remote = RetrofitClient.api_vehiculo.createVehiculo(vehiculo)
            persistRemote(remote, localId, vehiculo.usuario?.id)
            vehiculoDao.getByLocalId(localId)?.toRemoteModel() ?: remote
        } catch (_: Exception) {
            syncManagerProvider().syncEntity(ni.edu.uam.autotrak.data.sync.SyncConstants.ENTITY_VEHICULO)
            vehiculoDao.getByLocalId(localId)?.toRemoteModel() ?: vehiculo
        }
    }

    override suspend fun updateVehiculo(id: Long, vehiculo: Vehiculo): Vehiculo {
        val existing = vehiculoDao.getByServerId(id)
        val localId = existing?.localId ?: vehiculoDao.insert(
            vehiculo.toRoomEntity().copy(
                serverId = id,
                usuarioId = vehiculo.usuario?.id ?: existing?.usuarioId,
                syncState = SyncState.PENDING_UPDATE
            )
        )

        if (existing != null) {
            vehiculoDao.update(
                vehiculo.toRoomEntity().copy(
                    localId = localId,
                    serverId = id,
                    usuarioId = vehiculo.usuario?.id ?: existing.usuarioId,
                    syncState = if (existing.syncState == SyncState.PENDING_CREATE) SyncState.PENDING_CREATE else SyncState.PENDING_UPDATE
                )
            )
        }

        return try {
            val remote = RetrofitClient.api_vehiculo.updateVehiculo(id, vehiculo)
            persistRemote(remote, localId, vehiculo.usuario?.id ?: existing?.usuarioId)
            vehiculoDao.getByLocalId(localId)?.toRemoteModel() ?: remote
        } catch (_: Exception) {
            syncManagerProvider().syncEntity(ni.edu.uam.autotrak.data.sync.SyncConstants.ENTITY_VEHICULO)
            vehiculoDao.getByLocalId(localId)?.toRemoteModel() ?: vehiculo
        }
    }

    override suspend fun deleteVehiculo(id: Long) {
        val existing = vehiculoDao.getByServerId(id)
        if (existing != null) {
            vehiculoDao.update(existing.copy(syncState = SyncState.PENDING_DELETE))
        }
        try {
            RetrofitClient.api_vehiculo.deleteVehiculo(id)
            existing?.let { vehiculoDao.delete(it) }
        } catch (_: Exception) {
            syncManagerProvider().syncEntity(ni.edu.uam.autotrak.data.sync.SyncConstants.ENTITY_VEHICULO)
        }
    }

    private suspend fun upsertFromRemote(remote: Vehiculo, usuarioIdFallback: Long?) {
        val existing = remote.id?.let { vehiculoDao.getByServerId(it) }
        val entity = remote.toRoomEntity().copy(
            localId = existing?.localId ?: 0,
            usuarioId = remote.usuario?.id ?: usuarioIdFallback ?: existing?.usuarioId,
            syncState = SyncState.SYNCED
        )
        if (existing == null) {
            vehiculoDao.insert(entity)
        } else {
            vehiculoDao.update(entity)
        }
    }

    private suspend fun persistRemote(remote: Vehiculo, localId: Long, usuarioIdFallback: Long?) {
        vehiculoDao.update(
            remote.toRoomEntity().copy(
                localId = localId,
                usuarioId = remote.usuario?.id ?: usuarioIdFallback,
                syncState = SyncState.SYNCED
            )
        )
    }
}
