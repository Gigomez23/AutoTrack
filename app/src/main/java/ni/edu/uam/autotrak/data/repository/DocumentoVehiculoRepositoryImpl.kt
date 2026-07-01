package ni.edu.uam.autotrak.data.repository

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ni.edu.uam.autotrak.data.local.dao.DocumentoVehiculoDao
import ni.edu.uam.autotrak.data.local.db.AppDatabase
import ni.edu.uam.autotrak.data.mapper.toCreateRequestModel
import ni.edu.uam.autotrak.data.mapper.toRemoteModel
import ni.edu.uam.autotrak.data.mapper.toRoomEntity
import ni.edu.uam.autotrak.data.remote.RetrofitClient
import ni.edu.uam.autotrak.data.remote.model.DocumentoVehiculo
import ni.edu.uam.autotrak.data.sync.SyncConstants
import ni.edu.uam.autotrak.data.sync.SyncManager
import ni.edu.uam.autotrak.data.sync.SyncState

class DocumentoVehiculoRepositoryImpl(
    private val database: AppDatabase,
    private val dao: DocumentoVehiculoDao,
    private val syncManagerProvider: () -> SyncManager
) : DocumentoVehiculoRepository {

    override fun observeByVehiculoId(vehiculoId: Long): Flow<List<DocumentoVehiculo>> {
        return dao.observeByVehiculoId(vehiculoId).map { entities ->
            entities.map { it.toRemoteModel() }
        }
    }

    override suspend fun refreshByVehiculoId(vehiculoId: Long) {
        if (vehiculoId <= 0) return
        syncManagerProvider().pushDocumentoVehiculo()
        val remoteList = RetrofitClient.api_documento_vehiculo.getDocumentosVehiculoByVehiculoId(vehiculoId)
        database.withTransaction {
            remoteList.forEach { remote ->
                upsertFromRemote(remote.toRoomEntity(vehiculoId))
            }
        }
    }

    override suspend fun create(vehiculoId: Long, documentoVehiculo: DocumentoVehiculo): DocumentoVehiculo {
        val localId = dao.insert(
            documentoVehiculo.toRoomEntity(vehiculoId).copy(syncState = SyncState.PENDING_CREATE)
        )

        if (vehiculoId <= 0) {
            syncManagerProvider().syncEntity(SyncConstants.ENTITY_DOCUMENTO_VEHICULO)
            return dao.getByLocalId(localId)?.toRemoteModel() ?: documentoVehiculo
        }

        return try {
            val remote = RetrofitClient.api_documento_vehiculo.createDocumentoVehiculo(
                documentoVehiculo.toCreateRequestModel().copy(vehiculoId = vehiculoId)
            )
            persistRemote(remote.toRoomEntity(vehiculoId), localId, vehiculoId)
            dao.getByLocalId(localId)?.toRemoteModel() ?: remote
        } catch (_: Exception) {
            syncManagerProvider().syncEntity(SyncConstants.ENTITY_DOCUMENTO_VEHICULO)
            dao.getByLocalId(localId)?.toRemoteModel() ?: documentoVehiculo
        }
    }

    override suspend fun update(id: Long, documentoVehiculo: DocumentoVehiculo): DocumentoVehiculo {
        if (id < 0) {
            val existingLocal = dao.getByLocalId(-id)
            if (existingLocal != null) {
                dao.update(
                    documentoVehiculo.toRoomEntity(existingLocal.vehiculoId).copy(
                        localId = existingLocal.localId,
                        serverId = existingLocal.serverId,
                        vehiculoId = documentoVehiculo.vehiculoId ?: existingLocal.vehiculoId,
                        syncState = existingLocal.syncState
                    )
                )
                return dao.getByLocalId(existingLocal.localId)?.toRemoteModel() ?: documentoVehiculo
            }
            return documentoVehiculo
        }

        val existing = dao.getByServerId(id)
        val vehiculoId = documentoVehiculo.vehiculoId ?: existing?.vehiculoId
        val localId = existing?.localId ?: dao.insert(
            documentoVehiculo.toRoomEntity(vehiculoId).copy(
                serverId = id,
                vehiculoId = vehiculoId,
                syncState = SyncState.PENDING_UPDATE
            )
        )

        if (existing != null) {
            dao.update(
                documentoVehiculo.toRoomEntity(vehiculoId).copy(
                    localId = localId,
                    serverId = id,
                    vehiculoId = vehiculoId,
                    syncState = if (existing.syncState == SyncState.PENDING_CREATE) {
                        SyncState.PENDING_CREATE
                    } else {
                        SyncState.PENDING_UPDATE
                    }
                )
            )
        }

        return try {
            val remote = RetrofitClient.api_documento_vehiculo.updateDocumentoVehiculo(id, documentoVehiculo.copy(id = id))
            persistRemote(remote.toRoomEntity(vehiculoId), localId, vehiculoId)
            dao.getByLocalId(localId)?.toRemoteModel() ?: remote
        } catch (_: Exception) {
            syncManagerProvider().syncEntity(SyncConstants.ENTITY_DOCUMENTO_VEHICULO)
            dao.getByLocalId(localId)?.toRemoteModel() ?: documentoVehiculo
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
            RetrofitClient.api_documento_vehiculo.deleteDocumentoVehiculo(id)
            database.withTransaction {
                existing?.let { dao.delete(it) }
            }
        } catch (_: Exception) {
            syncManagerProvider().syncEntity(SyncConstants.ENTITY_DOCUMENTO_VEHICULO)
        }
    }

    private suspend fun upsertFromRemote(entity: ni.edu.uam.autotrak.data.local.model.DocumentoVehiculoEntity) {
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

    private suspend fun persistRemote(entity: ni.edu.uam.autotrak.data.local.model.DocumentoVehiculoEntity, localId: Long, vehiculoIdFallback: Long?) {
        database.withTransaction {
            dao.update(
                entity.copy(
                    localId = localId,
                    vehiculoId = entity.vehiculoId ?: vehiculoIdFallback,
                    syncState = SyncState.SYNCED
                )
            )
        }
    }
}
