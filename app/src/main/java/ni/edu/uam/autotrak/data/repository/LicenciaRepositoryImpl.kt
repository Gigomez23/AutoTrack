package ni.edu.uam.autotrak.data.repository

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ni.edu.uam.autotrak.data.local.dao.LicenciaDao
import ni.edu.uam.autotrak.data.local.db.AppDatabase
import ni.edu.uam.autotrak.data.mapper.toCreateRequestModel
import ni.edu.uam.autotrak.data.mapper.toRemoteModel
import ni.edu.uam.autotrak.data.mapper.toRoomEntity
import ni.edu.uam.autotrak.data.remote.RetrofitClient
import ni.edu.uam.autotrak.data.remote.model.Licencia
import ni.edu.uam.autotrak.data.sync.SyncConstants
import ni.edu.uam.autotrak.data.sync.SyncManager
import ni.edu.uam.autotrak.data.sync.SyncState

class LicenciaRepositoryImpl(
    private val database: AppDatabase,
    private val dao: LicenciaDao,
    private val syncManagerProvider: () -> SyncManager
) : LicenciaRepository {

    override fun observeByUsuarioId(usuarioId: Long): Flow<List<Licencia>> {
        return dao.observeByUsuarioId(usuarioId).map { entities ->
            entities.map { it.toRemoteModel() }
        }
    }

    override suspend fun refreshByUsuarioId(usuarioId: Long) {
        if (usuarioId <= 0) return
        syncManagerProvider().pushLicencia()
        try {
            val remote = RetrofitClient.api_licencia.getLicenciaByUsuarioId(usuarioId)
            database.withTransaction {
                upsertFromRemote(remote.toRoomEntity(usuarioId))
            }
        } catch (e: Exception) {
            // Manejar caso donde no hay licencia (404 probablemente)
        }
    }

    override suspend fun create(licencia: Licencia): Licencia {
        val localId = dao.insert(
            licencia.toRoomEntity().copy(syncState = SyncState.PENDING_CREATE)
        )

        val usuarioId = licencia.usuarioId
        if (usuarioId == null || usuarioId <= 0) {
            syncManagerProvider().syncEntity(SyncConstants.ENTITY_LICENCIA)
            return dao.getByLocalId(localId)?.toRemoteModel() ?: licencia
        }

        return try {
            val remote = RetrofitClient.api_licencia.createLicencia(licencia.toCreateRequestModel())
            persistRemote(remote.toRoomEntity(usuarioId), localId, usuarioId)
            dao.getByLocalId(localId)?.toRemoteModel() ?: remote
        } catch (_: Exception) {
            syncManagerProvider().syncEntity(SyncConstants.ENTITY_LICENCIA)
            dao.getByLocalId(localId)?.toRemoteModel() ?: licencia
        }
    }

    override suspend fun update(id: Long, licencia: Licencia): Licencia {
        if (id < 0) {
            val existingLocal = dao.getByLocalId(-id)
            if (existingLocal != null) {
                dao.update(
                    licencia.toRoomEntity(existingLocal.usuarioId).copy(
                        localId = existingLocal.localId,
                        serverId = existingLocal.serverId,
                        usuarioId = licencia.usuarioId ?: existingLocal.usuarioId,
                        syncState = existingLocal.syncState
                    )
                )
                return dao.getByLocalId(existingLocal.localId)?.toRemoteModel() ?: licencia
            }
            return licencia
        }

        val existing = dao.getByServerId(id)
        val usuarioId = licencia.usuarioId ?: existing?.usuarioId
        val localId = existing?.localId ?: dao.insert(
            licencia.toRoomEntity(usuarioId).copy(
                serverId = id,
                usuarioId = usuarioId,
                syncState = SyncState.PENDING_UPDATE
            )
        )

        if (existing != null) {
            dao.update(
                licencia.toRoomEntity(usuarioId).copy(
                    localId = localId,
                    serverId = id,
                    usuarioId = usuarioId,
                    syncState = if (existing.syncState == SyncState.PENDING_CREATE) {
                        SyncState.PENDING_CREATE
                    } else {
                        SyncState.PENDING_UPDATE
                    }
                )
            )
        }

        return try {
            val remote = RetrofitClient.api_licencia.updateLicencia(id, licencia.copy(id = id))
            persistRemote(remote.toRoomEntity(usuarioId), localId, usuarioId)
            dao.getByLocalId(localId)?.toRemoteModel() ?: remote
        } catch (_: Exception) {
            syncManagerProvider().syncEntity(SyncConstants.ENTITY_LICENCIA)
            dao.getByLocalId(localId)?.toRemoteModel() ?: licencia
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
            RetrofitClient.api_licencia.deleteLicencia(id)
            database.withTransaction {
                existing?.let { dao.delete(it) }
            }
        } catch (_: Exception) {
            syncManagerProvider().syncEntity(SyncConstants.ENTITY_LICENCIA)
        }
    }

    private suspend fun upsertFromRemote(entity: ni.edu.uam.autotrak.data.local.model.LicenciaEntity) {
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

    private suspend fun persistRemote(entity: ni.edu.uam.autotrak.data.local.model.LicenciaEntity, localId: Long, usuarioIdFallback: Long?) {
        database.withTransaction {
            dao.update(
                entity.copy(
                    localId = localId,
                    usuarioId = entity.usuarioId ?: usuarioIdFallback,
                    syncState = SyncState.SYNCED
                )
            )
        }
    }
}
