package ni.edu.uam.autotrak.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ni.edu.uam.autotrak.data.local.dao.UsuarioDao
import ni.edu.uam.autotrak.data.mapper.toRemoteModel
import ni.edu.uam.autotrak.data.mapper.toRoomEntity
import ni.edu.uam.autotrak.data.remote.api.UsuarioApi
import ni.edu.uam.autotrak.data.remote.model.LoginRequest
import ni.edu.uam.autotrak.data.remote.model.LoginResponse
import ni.edu.uam.autotrak.data.remote.model.Usuario
import ni.edu.uam.autotrak.data.sync.SyncManager
import ni.edu.uam.autotrak.data.sync.SyncState

class UsuarioRepositoryImpl(
    private val usuarioApi: UsuarioApi,
    private val usuarioDao: UsuarioDao,
    private val syncManagerProvider: () -> SyncManager
) : UsuarioRepository {

    override suspend fun login(request: LoginRequest): LoginResponse {
        return usuarioApi.login(request)
    }

    override fun observeUsuario(id: Long): Flow<Usuario?> {
        return usuarioDao.observeAll().map { users ->
            users.find { it.serverId == id }?.toRemoteModel()
        }
    }

    override suspend fun refreshUsuario(id: Long) {
        try {
            syncManagerProvider().pushUsuario()
            val remote = usuarioApi.getUsuario(id)
            upsertFromRemote(remote)
        } catch (e: Exception) {
            // If targeted pull fails, try a general sync as fallback or rethrow
            syncManagerProvider().pullUsuario()
            throw e
        }
    }

    override suspend fun createUsuario(usuario: Usuario): Usuario {
        val localId = usuarioDao.insert(
            usuario.toRoomEntity().copy(syncState = SyncState.PENDING_CREATE)
        )
        return try {
            val remote = usuarioApi.createUsuario(usuario)
            persistRemote(remote, localId)
            usuarioDao.getByLocalId(localId)?.toRemoteModel() ?: remote
        } catch (_: Exception) {
            syncManagerProvider().syncEntity(ni.edu.uam.autotrak.data.sync.SyncConstants.ENTITY_USUARIO)
            usuarioDao.getByLocalId(localId)?.toRemoteModel() ?: usuario
        }
    }

    override suspend fun updateUsuario(id: Long, usuario: Usuario): Usuario {
        val existing = usuarioDao.getByServerId(id)
        val localId = existing?.localId ?: usuarioDao.insert(
            usuario.toRoomEntity().copy(
                serverId = id,
                syncState = SyncState.PENDING_UPDATE
            )
        )

        if (existing != null) {
            usuarioDao.update(
                usuario.toRoomEntity().copy(
                    localId = localId,
                    serverId = id,
                    syncState = if (existing.syncState == SyncState.PENDING_CREATE) SyncState.PENDING_CREATE else SyncState.PENDING_UPDATE
                )
            )
        }

        return try {
            val remote = usuarioApi.updateUsuario(id, usuario)
            persistRemote(remote, localId)
            usuarioDao.getByLocalId(localId)?.toRemoteModel() ?: remote
        } catch (_: Exception) {
            syncManagerProvider().syncEntity(ni.edu.uam.autotrak.data.sync.SyncConstants.ENTITY_USUARIO)
            usuarioDao.getByLocalId(localId)?.toRemoteModel() ?: usuario
        }
    }

    override suspend fun deleteUsuario(id: Long) {
        val existing = usuarioDao.getByServerId(id)
        if (existing != null) {
            usuarioDao.update(existing.copy(syncState = SyncState.PENDING_DELETE))
        }
        try {
            usuarioApi.deleteUsuario(id)
            existing?.let { usuarioDao.delete(it) }
        } catch (_: Exception) {
            syncManagerProvider().syncEntity(ni.edu.uam.autotrak.data.sync.SyncConstants.ENTITY_USUARIO)
        }
    }

    private suspend fun upsertFromRemote(remote: Usuario) {
        val existing = remote.id?.let { usuarioDao.getByServerId(it) }
        val entity = remote.toRoomEntity().copy(
            localId = existing?.localId ?: 0,
            syncState = SyncState.SYNCED
        )
        if (existing == null) {
            usuarioDao.insert(entity)
        } else {
            usuarioDao.update(entity)
        }
    }

    private suspend fun persistRemote(remote: Usuario, localId: Long) {
        usuarioDao.update(
            remote.toRoomEntity().copy(
                localId = localId,
                syncState = SyncState.SYNCED
            )
        )
    }
}
