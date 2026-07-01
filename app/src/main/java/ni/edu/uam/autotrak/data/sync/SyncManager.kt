package ni.edu.uam.autotrak.data.sync

import android.util.Log
import androidx.room.withTransaction
import ni.edu.uam.autotrak.data.local.dao.DocumentoDao
import ni.edu.uam.autotrak.data.local.dao.LicenciaDao
import ni.edu.uam.autotrak.data.local.dao.RegistroCombustibleDao
import ni.edu.uam.autotrak.data.local.dao.RegistroDao
import ni.edu.uam.autotrak.data.local.dao.RegistroProblemaDao
import ni.edu.uam.autotrak.data.local.dao.SyncMetadataDao
import ni.edu.uam.autotrak.data.local.dao.UsuarioDao
import ni.edu.uam.autotrak.data.local.dao.VehiculoDao
import ni.edu.uam.autotrak.data.local.db.AppDatabase
import ni.edu.uam.autotrak.data.local.model.SyncMetadataEntity
import ni.edu.uam.autotrak.data.mapper.toRemoteModel
import ni.edu.uam.autotrak.data.mapper.toCreateRequestModel
import ni.edu.uam.autotrak.data.mapper.toRoomEntity
import ni.edu.uam.autotrak.data.mapper.toSyncDto
import ni.edu.uam.autotrak.data.remote.api.DocumentoApi
import ni.edu.uam.autotrak.data.remote.api.LicenciaApi
import ni.edu.uam.autotrak.data.remote.api.RegistroApi
import ni.edu.uam.autotrak.data.remote.api.RegistroCombustibleApi
import ni.edu.uam.autotrak.data.remote.api.RegistroProblemaApi
import ni.edu.uam.autotrak.data.remote.api.UsuarioApi
import ni.edu.uam.autotrak.data.remote.api.VehiculoApi
import ni.edu.uam.autotrak.data.sync.SyncConstants.ENTITY_REGISTRO_COMBUSTIBLE
import ni.edu.uam.autotrak.data.sync.SyncConstants.ENTITY_LICENCIA
import ni.edu.uam.autotrak.data.sync.SyncConstants.ENTITY_DOCUMENTO
import ni.edu.uam.autotrak.data.sync.SyncConstants.ENTITY_REGISTRO_PROBLEMA
import ni.edu.uam.autotrak.data.sync.SyncConstants.ENTITY_USUARIO
import ni.edu.uam.autotrak.data.sync.SyncConstants.ENTITY_VEHICULO

class SyncManager(
    private val database: AppDatabase,
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
    private val registroDao: RegistroDao,
    private val licenciaApi: LicenciaApi,
    private val licenciaDao: LicenciaDao,
    private val documentoApi: DocumentoApi,
    private val documentoDao: DocumentoDao
) {

    suspend fun syncAll() {
        runSyncStep { pushLocalChanges() }
        runSyncStep { pullRemoteChanges() }
    }

    suspend fun syncEntity(entityName: String) {
        when (entityName) {
            ENTITY_USUARIO -> {
                pushUsuario()
                pullUsuario()
            }
            ENTITY_VEHICULO -> {
                pushVehiculo()
                pullVehiculo()
            }
            ENTITY_REGISTRO_COMBUSTIBLE -> {
                pushFuel()
                pullFuel()
            }
            ENTITY_REGISTRO_PROBLEMA -> {
                pushProblems()
                pullProblems()
            }
            ENTITY_LICENCIA -> {
                pushLicencia()
                pullLicencia()
            }
            ENTITY_DOCUMENTO -> {
                pushDocumentos()
                pullDocumentos()
            }
        }
    }

    suspend fun relinkVehicleChildren(oldVehicleId: Long, newVehicleId: Long) {
        if (oldVehicleId == newVehicleId) return
        database.withTransaction {
            registroDao.updateVehiculoId(oldVehicleId, newVehicleId)
            fuelDao.updateVehiculoId(oldVehicleId, newVehicleId)
            problemDao.updateVehiculoId(oldVehicleId, newVehicleId)
        }
    }

    private suspend fun pushLocalChanges() {
        runSyncStep { pushUsuario() }
        runSyncStep { pushVehiculo() }
        runSyncStep { pushFuel() }
        runSyncStep { pushProblems() }
        runSyncStep { pushRegistros() }
        runSyncStep { pushLicencia() }
        runSyncStep { pushDocumentos() }
    }

    private suspend fun pullRemoteChanges() {
        runSyncStep { pullUsuario() }
        runSyncStep { pullVehiculo() }
        runSyncStep { pullFuel() }
        runSyncStep { pullProblems() }
        runSyncStep { pullRegistros() }
        runSyncStep { pullLicencia() }
        runSyncStep { pullDocumentos() }
    }

    private suspend inline fun runSyncStep(crossinline block: suspend () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            Log.e("SyncManager", "Sync step failed", e)
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
                        database.withTransaction { usuarioDao.delete(local) }
                        null
                    }
                    else -> null
                }
                remote?.let {
                    database.withTransaction {
                        usuarioDao.update(
                            it.toRoomEntity().copy(
                                localId = local.localId,
                                syncState = SyncState.SYNCED
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("SyncManager", "Failed to push usuario ${local.localId}", e)
            }
        }
    }

    suspend fun pullUsuario() {
        val lastSync = syncMetadataDao.getByEntityName(ENTITY_USUARIO)?.lastSuccessfulSyncServerTimeMillis ?: 0L
        try {
            val remoteList = usuarioApi.getUpdatedAfter(lastSync)
            database.withTransaction {
                remoteList.forEach { remote ->
                    val existing = remote.id?.let { usuarioDao.getByServerId(it) }
                    if (remote.eliminado) {
                        existing?.let { usuarioDao.delete(it) }
                    } else {
                        val next = remote.toRoomEntity().copy(
                            localId = existing?.localId ?: 0,
                            syncState = SyncState.SYNCED
                        )
                        if (existing == null) {
                            usuarioDao.insert(next)
                        } else if (existing.syncState == SyncState.SYNCED) {
                            if (remote.fechaActualizacion == null || existing.fechaActualizacion == null ||
                                remote.fechaActualizacion.isAfter(existing.fechaActualizacion)
                            ) {
                                usuarioDao.update(next)
                            }
                        }
                    }
                }
                syncMetadataDao.upsert(SyncMetadataEntity(ENTITY_USUARIO, System.currentTimeMillis()))
            }
        } catch (e: Exception) {
            Log.e("SyncManager", "Failed to pull usuarios", e)
        }
    }

    // --- VEHICULO ---
    suspend fun pushVehiculo() {
        vehiculoDao.getPendingSync().forEach { local ->
            try {
                val localUser = local.usuarioId?.let { usuarioDao.getByServerId(it)?.toRemoteModel() }
                val createPayload = local.toRemoteModel().copy(
                    id = null,
                    usuario = localUser ?: local.toRemoteModel().usuario
                ).toCreateRequestModel()
                val remote = when (local.syncState) {
                    SyncState.PENDING_CREATE -> vehiculoApi.createVehiculo(createPayload)
                    SyncState.PENDING_UPDATE -> local.serverId?.let { vehiculoApi.updateVehiculo(it, local.toRemoteModel()) }
                    SyncState.PENDING_DELETE -> {
                        local.serverId?.let { vehiculoApi.deleteVehiculo(it) }
                        database.withTransaction { vehiculoDao.delete(local) }
                        null
                    }
                    else -> null
                }
                remote?.let {
                    database.withTransaction {
                        vehiculoDao.update(
                            it.toRoomEntity().copy(
                                localId = local.localId,
                                usuarioId = it.usuario?.id ?: local.usuarioId,
                                syncState = SyncState.SYNCED
                            )
                        )
                    }
                    if (local.serverId == null) {
                        it.id?.let { newServerId -> relinkVehicleChildren(-local.localId, newServerId) }
                    }
                }
            } catch (e: Exception) {
                Log.e("SyncManager", "Failed to push vehiculo ${local.localId}", e)
            }
        }
    }

    suspend fun pullVehiculo() {
        val lastSync = syncMetadataDao.getByEntityName(ENTITY_VEHICULO)?.lastSuccessfulSyncServerTimeMillis ?: 0L
        try {
            val remoteList = vehiculoApi.getUpdatedAfter(lastSync)
            database.withTransaction {
                remoteList.forEach { remote ->
                    val existing = remote.id?.let { vehiculoDao.getByServerId(it) }
                    if (remote.eliminado) {
                        existing?.let { vehiculoDao.delete(it) }
                    } else {
                        val next = remote.toRoomEntity().copy(
                            localId = existing?.localId ?: 0,
                            usuarioId = remote.usuario?.id ?: existing?.usuarioId,
                            syncState = SyncState.SYNCED
                        )
                        if (existing == null) {
                            vehiculoDao.insert(next)
                        } else if (existing.syncState == SyncState.SYNCED) {
                            if (remote.fechaActualizacion == null || existing.fechaActualizacion == null ||
                                remote.fechaActualizacion.isAfter(existing.fechaActualizacion)
                            ) {
                                vehiculoDao.update(next)
                            }
                        }
                    }
                }
                syncMetadataDao.upsert(SyncMetadataEntity(ENTITY_VEHICULO, System.currentTimeMillis()))
            }
        } catch (e: Exception) {
            Log.e("SyncManager", "Failed to pull vehiculos", e)
        }
    }

    // --- FUEL ---
    suspend fun pushFuel() {
        fuelDao.getPendingSync().forEach { local ->
            try {
                val parentId = local.vehiculoId
                val remote = when (local.syncState) {
                    SyncState.PENDING_CREATE -> parentId?.takeIf { it > 0 }?.let { fuelApi.createRegistroCombustible(it, local.toRemoteModel()) }
                    SyncState.PENDING_UPDATE -> local.serverId?.takeIf { parentId != null && parentId > 0 }?.let { fuelApi.updateRegistroCombustible(it, local.toRemoteModel()) }
                    SyncState.PENDING_DELETE -> {
                        local.serverId?.let { registroApi.deleteRegistro(it) }
                        database.withTransaction { fuelDao.delete(local) }
                        null
                    }
                    else -> null
                }
                remote?.let {
                    database.withTransaction {
                        fuelDao.update(
                            it.toRoomEntity(local.vehiculoId).copy(
                                localId = local.localId,
                                syncState = SyncState.SYNCED
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("SyncManager", "Failed to push fuel ${local.localId}", e)
            }
        }
    }

    suspend fun pullFuel() {
        val lastSync = syncMetadataDao.getByEntityName(ENTITY_REGISTRO_COMBUSTIBLE)?.lastSuccessfulSyncServerTimeMillis ?: 0L
        try {
            val remoteList = fuelApi.getUpdatedAfter(lastSync)
            database.withTransaction {
                remoteList.forEach { remote ->
                    val existing = remote.id?.let { fuelDao.getByServerId(it) }
                    if (remote.eliminado) {
                        existing?.let { fuelDao.delete(it) }
                    } else {
                        val next = remote.toRoomEntity(existing?.vehiculoId).copy(
                            localId = existing?.localId ?: 0,
                            syncState = SyncState.SYNCED
                        )
                        if (existing == null) {
                            fuelDao.insert(next)
                        } else if (existing.syncState == SyncState.SYNCED) {
                            if (remote.fechaActualizacion == null || existing.fechaActualizacion == null ||
                                remote.fechaActualizacion.isAfter(existing.fechaActualizacion)
                            ) {
                                fuelDao.update(next)
                            }
                        }
                    }
                }
                syncMetadataDao.upsert(SyncMetadataEntity(ENTITY_REGISTRO_COMBUSTIBLE, System.currentTimeMillis()))
            }
        } catch (e: Exception) {
            Log.e("SyncManager", "Failed to pull fuel records", e)
        }
    }

    // --- PROBLEMS ---
    suspend fun pushProblems() {
        problemDao.getPendingSync().forEach { local ->
            try {
                val parentId = local.vehiculoId
                val remote = when (local.syncState) {
                    SyncState.PENDING_CREATE -> parentId?.takeIf { it > 0 }?.let { problemApi.createRegistroProblema(it, local.toRemoteModel()) }
                    SyncState.PENDING_UPDATE -> local.serverId?.takeIf { parentId != null && parentId > 0 }?.let { problemApi.updateRegistroProblema(it, local.toRemoteModel()) }
                    SyncState.PENDING_DELETE -> {
                        local.serverId?.let { registroApi.deleteRegistro(it) }
                        database.withTransaction { problemDao.delete(local) }
                        null
                    }
                    else -> null
                }
                remote?.let {
                    database.withTransaction {
                        problemDao.update(
                            it.toRoomEntity(local.vehiculoId).copy(
                                localId = local.localId,
                                syncState = SyncState.SYNCED
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("SyncManager", "Failed to push problem ${local.localId}", e)
            }
        }
    }

    suspend fun pullProblems() {
        val lastSync = syncMetadataDao.getByEntityName(ENTITY_REGISTRO_PROBLEMA)?.lastSuccessfulSyncServerTimeMillis ?: 0L
        try {
            val remoteList = problemApi.getUpdatedAfter(lastSync)
            database.withTransaction {
                remoteList.forEach { remote ->
                    val existing = remote.id?.let { problemDao.getByServerId(it) }
                    if (remote.eliminado) {
                        existing?.let { problemDao.delete(it) }
                    } else {
                        val next = remote.toRoomEntity(existing?.vehiculoId).copy(
                            localId = existing?.localId ?: 0,
                            syncState = SyncState.SYNCED
                        )
                        if (existing == null) {
                            problemDao.insert(next)
                        } else if (existing.syncState == SyncState.SYNCED) {
                            if (remote.fechaActualizacion == null || existing.fechaActualizacion == null ||
                                remote.fechaActualizacion.isAfter(existing.fechaActualizacion)
                            ) {
                                problemDao.update(next)
                            }
                        }
                    }
                }
                syncMetadataDao.upsert(SyncMetadataEntity(ENTITY_REGISTRO_PROBLEMA, System.currentTimeMillis()))
            }
        } catch (e: Exception) {
            Log.e("SyncManager", "Failed to pull problems", e)
        }
    }

    // --- REGISTRO GENERAL ---
    private suspend fun pushRegistros() {
        registroDao.getPendingSync().forEach { local ->
            try {
                if (local.syncState.isDeleteRetryState()) {
                    local.serverId?.let { registroApi.deleteRegistro(it) }
                    database.withTransaction { registroDao.delete(local) }
                }
            } catch (e: Exception) {
                Log.e("SyncManager", "Failed to push registro ${local.localId}", e)
            }
        }
    }

    private fun pullRegistros() {
        // No-op: this entity is not currently synchronized by the backend flow.
    }

    // --- LICENCIAS ---
    suspend fun pushLicencia() {
        licenciaDao.getPendingSync().forEach { local ->
            try {
                val remote = when (local.syncState) {
                    SyncState.PENDING_CREATE -> licenciaApi.createLicencia(local.toRemoteModel().copy(id = null))
                    SyncState.PENDING_UPDATE -> local.serverId?.let { licenciaApi.updateLicencia(it, local.toRemoteModel()) }
                    SyncState.PENDING_DELETE -> {
                        local.serverId?.let { licenciaApi.deleteLicencia(it) }
                        database.withTransaction { licenciaDao.delete(local) }
                        null
                    }
                    else -> null
                }
                remote?.let {
                    database.withTransaction {
                        licenciaDao.update(
                            it.toRoomEntity(it.usuarioId ?: local.usuarioId).copy(
                                localId = local.localId,
                                syncState = SyncState.SYNCED
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("SyncManager", "Failed to push licencia ${local.localId}", e)
            }
        }
    }

    suspend fun pullLicencia() {
        val lastSync = syncMetadataDao.getByEntityName(ENTITY_LICENCIA)?.lastSuccessfulSyncServerTimeMillis ?: 0L
        try {
            val remoteList = licenciaApi.getUpdatedAfter(lastSync)
            database.withTransaction {
                remoteList.forEach { remote ->
                    val existing = remote.id?.let { licenciaDao.getByServerId(it) }
                    if (remote.eliminado) {
                        existing?.let { licenciaDao.delete(it) }
                    } else {
                        val next = remote.toRoomEntity(remote.usuarioId ?: existing?.usuarioId).copy(
                            localId = existing?.localId ?: 0,
                            syncState = SyncState.SYNCED
                        )
                        if (existing == null) {
                            licenciaDao.insert(next)
                        } else if (existing.syncState == SyncState.SYNCED) {
                            if (remote.fechaActualizacion == null || existing.fechaActualizacion == null ||
                                remote.fechaActualizacion.isAfter(existing.fechaActualizacion)
                            ) {
                                licenciaDao.update(next)
                            }
                        }
                    }
                }
                syncMetadataDao.upsert(SyncMetadataEntity(ENTITY_LICENCIA, System.currentTimeMillis()))
            }
        } catch (e: Exception) {
            Log.e("SyncManager", "Failed to pull licencias", e)
        }
    }


    //--- DOCUMENTOS ---
    private suspend fun pushDocumentos() {
        documentoDao.getPendingSync().forEach { local ->
            try {
                val remote = when (local.syncState) {
                    SyncState.PENDING_CREATE -> documentoApi.updateDocumento(local.toRemoteModel().copy(id = null))
                    SyncState.PENDING_UPDATE -> documentoApi.updateDocumento(local.toRemoteModel())
                    SyncState.PENDING_DELETE -> {
                        documentoApi.deleteDocumento(local.toSyncDto(eliminado = true))
                        database.withTransaction { documentoDao.delete(local) }
                        null
                    }
                    else -> null
                }
                remote?.let {
                    database.withTransaction {
                        documentoDao.update(
                            it.toRoomEntity().copy(
                                localId = local.localId,
                                syncState = SyncState.SYNCED
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("SyncManager", "Failed to push documento ${local.localId}", e)
            }
        }
    }

    private suspend fun pullDocumentos() {
        val lastSync = syncMetadataDao.getByEntityName(ENTITY_DOCUMENTO)?.lastSuccessfulSyncServerTimeMillis ?: 0L
        try {
            val remoteList = documentoApi.getUpdatedAfter(lastSync)
            database.withTransaction {
                remoteList.forEach { remote ->
                    val existing = remote.id?.let { documentoDao.getByServerId(it) }
                    if (remote.eliminado) {
                        existing?.let { documentoDao.delete(it) }
                    } else {
                        val next = remote.toRoomEntity().copy(
                            localId = existing?.localId ?: 0,
                            syncState = SyncState.SYNCED
                        )
                        if (existing == null) {
                            documentoDao.insert(next)
                        } else if (existing.syncState == SyncState.SYNCED) {
                            if (remote.fechaActualizacion == null || existing.fechaActualizacion == null ||
                                remote.fechaActualizacion.isAfter(existing.fechaActualizacion)
                            ) {
                                documentoDao.update(next)
                            }
                        }
                    }
                }
                syncMetadataDao.upsert(SyncMetadataEntity(ENTITY_DOCUMENTO, System.currentTimeMillis()))
            }
        } catch (e: Exception) {
            Log.e("SyncManager", "Failed to pull documentos", e)
        }
    }



}
