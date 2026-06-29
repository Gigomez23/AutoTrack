package ni.edu.uam.autotrak.data.sync

import ni.edu.uam.autotrak.data.local.dao.*
import ni.edu.uam.autotrak.data.local.model.*
import ni.edu.uam.autotrak.data.mapper.*
import ni.edu.uam.autotrak.data.remote.api.*

class SyncManager(
    private val syncMetadataDao: SyncMetadataDao,
    private val usuarioApi: UsuarioApi,
    private val usuarioDao: UsuarioDao,
    private val vehiculoApi: VehiculoApi,
    private val vehiculoDao: VehiculoDao,
    private val fuelApi: RegistroCombustibleApi,
    private val fuelDao: RegistroCombustibleDao,
    private val problemApi: RegistroProblemaApi,
    private val problemDao: RegistroProblemaDao,
    private val registroApi: RegistroApi,
    private val registroDao: RegistroDao
) {

    suspend fun syncAll() {
        runSyncStep { pushLocalChanges() }
        runSyncStep { pullRemoteChanges() }
    }

    suspend fun syncEntity(entityName: String) {
        when (entityName) {
            SyncConstants.ENTITY_USUARIO -> {
                pushUsuario()
                pullUsuario()
            }
            SyncConstants.ENTITY_VEHICULO -> {
                pushVehiculo()
                pullVehiculo()
            }
            SyncConstants.ENTITY_REGISTRO_COMBUSTIBLE -> {
                pushFuel()
                pullFuel()
            }
            SyncConstants.ENTITY_REGISTRO_PROBLEMA -> {
                pushProblems()
                pullProblems()
            }
        }
    }

    private suspend fun pushLocalChanges() {
        runSyncStep { pushUsuario() }
        runSyncStep { pushVehiculo() }
        runSyncStep { pushFuel() }
        runSyncStep { pushProblems() }
        runSyncStep { pushRegistros() }
    }

    private suspend fun pullRemoteChanges() {
        runSyncStep { pullUsuario() }
        runSyncStep { pullVehiculo() }
        runSyncStep { pullFuel() }
        runSyncStep { pullProblems() }
        runSyncStep { pullRegistros() }
    }

    private suspend inline fun runSyncStep(crossinline block: suspend () -> Unit) {
        try {
            block()
        } catch (_: Exception) {
            // Keep processing the remaining entities.
        }
    }

    private fun SyncState.isDeleteRetryState(): Boolean {
        return this == SyncState.PENDING_DELETE || this == SyncState.SYNC_FAILED
    }

    // --- USUARIO ---
    suspend fun pushUsuario() {
        usuarioDao.getPendingSync().forEach { local ->
            try {
                val remote = when (local.syncState) {
                    SyncState.PENDING_CREATE -> usuarioApi.createUsuario(local.toRemoteModel())
                    SyncState.PENDING_UPDATE -> local.serverId?.let { usuarioApi.updateUsuario(it, local.toRemoteModel()) }
                    SyncState.PENDING_DELETE -> {
                        local.serverId?.let { usuarioApi.deleteUsuario(it) }
                        usuarioDao.delete(local)
                        null
                    }
                    else -> null
                }
                remote?.let {
                    val entity = it.toRoomEntity().copy(
                        localId = local.localId,
                        syncState = SyncState.SYNCED
                    )
                    usuarioDao.update(entity)
                }
            } catch (e: Exception) {}
        }
    }

    suspend fun pullUsuario() {
        val lastSync = syncMetadataDao.getByEntityName(SyncConstants.ENTITY_USUARIO)?.lastSuccessfulSyncServerTimeMillis ?: 0L
        try {
            val remoteList = usuarioApi.getUpdatedAfter(lastSync)
            remoteList.forEach { remote ->
                val existing = remote.id?.let { usuarioDao.getByServerId(it) }
                
                if (remote.eliminado) {
                    existing?.let { usuarioDao.delete(it) }
                } else {
                    if (existing == null) {
                        usuarioDao.insert(remote.toRoomEntity())
                    } else if (existing.syncState == SyncState.SYNCED) {
                        // Only pull if local is not pending changes, or use conflict resolution
                        if (remote.fechaActualizacion == null || existing.fechaActualizacion == null || 
                            remote.fechaActualizacion.isAfter(existing.fechaActualizacion)) {
                            usuarioDao.update(remote.toRoomEntity().copy(localId = existing.localId))
                        }
                    }
                }
            }
            syncMetadataDao.upsert(SyncMetadataEntity(SyncConstants.ENTITY_USUARIO, System.currentTimeMillis()))
        } catch (e: Exception) {}
    }

    // --- VEHICULO ---
    suspend fun pushVehiculo() {
        vehiculoDao.getPendingSync().forEach { local ->
            try {
                val remote = when (local.syncState) {
                    SyncState.PENDING_CREATE -> vehiculoApi.createVehiculo(local.toRemoteModel())
                    SyncState.PENDING_UPDATE -> local.serverId?.let { vehiculoApi.updateVehiculo(it, local.toRemoteModel()) }
                    SyncState.PENDING_DELETE -> {
                        local.serverId?.let { vehiculoApi.deleteVehiculo(it) }
                        vehiculoDao.delete(local)
                        null
                    }
                    else -> null
                }
                remote?.let {
                    val entity = it.toRoomEntity().copy(
                        localId = local.localId,
                        usuarioId = it.usuario?.id ?: local.usuarioId,
                        syncState = SyncState.SYNCED
                    )
                    vehiculoDao.update(entity)
                }
            } catch (e: Exception) {}
        }
    }

    suspend fun pullVehiculo() {
        val lastSync = syncMetadataDao.getByEntityName(SyncConstants.ENTITY_VEHICULO)?.lastSuccessfulSyncServerTimeMillis ?: 0L
        try {
            val remoteList = vehiculoApi.getUpdatedAfter(lastSync)
            remoteList.forEach { remote ->
                val existing = remote.id?.let { vehiculoDao.getByServerId(it) }
                
                if (remote.eliminado) {
                    existing?.let { vehiculoDao.delete(it) }
                } else {
                    if (existing == null) {
                        vehiculoDao.insert(remote.toRoomEntity())
                    } else if (existing.syncState == SyncState.SYNCED) {
                        if (remote.fechaActualizacion == null || existing.fechaActualizacion == null || 
                            remote.fechaActualizacion.isAfter(existing.fechaActualizacion)) {
                            val entity = remote.toRoomEntity().copy(
                                localId = existing.localId,
                                usuarioId = remote.usuario?.id ?: existing.usuarioId
                            )
                            vehiculoDao.update(entity)
                        }
                    }
                }
            }
            syncMetadataDao.upsert(SyncMetadataEntity(SyncConstants.ENTITY_VEHICULO, System.currentTimeMillis()))
        } catch (e: Exception) {}
    }

    // --- FUEL ---
    suspend fun pushFuel() {
        fuelDao.getPendingSync().forEach { local ->
            try {
                val remote = when (local.syncState) {
                    SyncState.PENDING_CREATE -> local.vehiculoId?.let { fuelApi.createRegistroCombustible(it, local.toRemoteModel()) }
                    SyncState.PENDING_UPDATE -> local.serverId?.let { fuelApi.updateRegistroCombustible(it, local.toRemoteModel()) }
                    SyncState.PENDING_DELETE -> {
                        local.serverId?.let { fuelApi.deleteRegistroCombustible(it) }
                        fuelDao.delete(local)
                        null
                    }
                    else -> null
                }
                remote?.let {
                    val entity = it.toRoomEntity(local.vehiculoId).copy(
                        localId = local.localId,
                        syncState = SyncState.SYNCED
                    )
                    fuelDao.update(entity)
                }
            } catch (e: Exception) {}
        }
    }

    suspend fun pullFuel() {
        val lastSync = syncMetadataDao.getByEntityName(SyncConstants.ENTITY_REGISTRO_COMBUSTIBLE)?.lastSuccessfulSyncServerTimeMillis ?: 0L
        try {
            val remoteList = fuelApi.getUpdatedAfter(lastSync)
            remoteList.forEach { remote ->
                val existing = remote.id?.let { fuelDao.getByServerId(it) }
                
                if (remote.eliminado) {
                    existing?.let { fuelDao.delete(it) }
                } else {
                    if (existing == null) {
                        fuelDao.insert(remote.toRoomEntity())
                    } else if (existing.syncState == SyncState.SYNCED) {
                        if (remote.fechaActualizacion == null || existing.fechaActualizacion == null || 
                            remote.fechaActualizacion.isAfter(existing.fechaActualizacion)) {
                            val entity = remote.toRoomEntity(existing.vehiculoId).copy(localId = existing.localId)
                            fuelDao.update(entity)
                        }
                    }
                }
            }
            syncMetadataDao.upsert(SyncMetadataEntity(SyncConstants.ENTITY_REGISTRO_COMBUSTIBLE, System.currentTimeMillis()))
        } catch (e: Exception) {}
    }

    // --- PROBLEMS ---
    suspend fun pushProblems() {
        problemDao.getPendingSync().forEach { local ->
            try {
                val remote = when (local.syncState) {
                    SyncState.PENDING_CREATE -> local.vehiculoId?.let { problemApi.createRegistroProblema(it, local.toRemoteModel()) }
                    SyncState.PENDING_UPDATE -> local.serverId?.let { problemApi.updateRegistroProblema(it, local.toRemoteModel()) }
                    SyncState.PENDING_DELETE -> {
                        local.serverId?.let { problemApi.deleteRegistroProblema(it) }
                        problemDao.delete(local)
                        null
                    }
                    else -> null
                }
                remote?.let {
                    val entity = it.toRoomEntity(local.vehiculoId).copy(
                        localId = local.localId,
                        syncState = SyncState.SYNCED
                    )
                    problemDao.update(entity)
                }
            } catch (e: Exception) {}
        }
    }

    suspend fun pullProblems() {
        val lastSync = syncMetadataDao.getByEntityName(SyncConstants.ENTITY_REGISTRO_PROBLEMA)?.lastSuccessfulSyncServerTimeMillis ?: 0L
        try {
            val remoteList = problemApi.getUpdatedAfter(lastSync)
            remoteList.forEach { remote ->
                val existing = remote.id?.let { problemDao.getByServerId(it) }
                
                if (remote.eliminado) {
                    existing?.let { problemDao.delete(it) }
                } else {
                    if (existing == null) {
                        problemDao.insert(remote.toRoomEntity())
                    } else if (existing.syncState == SyncState.SYNCED) {
                        if (remote.fechaActualizacion == null || existing.fechaActualizacion == null || 
                            remote.fechaActualizacion.isAfter(existing.fechaActualizacion)) {
                            val entity = remote.toRoomEntity(existing.vehiculoId).copy(localId = existing.localId)
                            problemDao.update(entity)
                        }
                    }
                }
            }
            syncMetadataDao.upsert(SyncMetadataEntity(SyncConstants.ENTITY_REGISTRO_PROBLEMA, System.currentTimeMillis()))
        } catch (e: Exception) {}
    }

    // --- REGISTRO GENERAL ---
    private suspend fun pushRegistros() {
        registroDao.getPendingSync().forEach { local ->
            try {
                if (local.syncState.isDeleteRetryState()) {
                    local.serverId?.let { registroApi.deleteRegistro(it) }
                    registroDao.delete(local)
                }
            } catch (e: Exception) {}
        }
    }

    private fun pullRegistros() {
        // ... simplified
    }
}
