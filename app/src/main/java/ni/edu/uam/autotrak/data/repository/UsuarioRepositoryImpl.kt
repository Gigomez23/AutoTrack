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

class UsuarioRepositoryImpl(
    private val usuarioApi: UsuarioApi,
    private val usuarioDao: UsuarioDao
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
            val remoteUser = usuarioApi.getUsuario(id)
            val existing = usuarioDao.getByServerId(id)
            if (existing != null) {
                usuarioDao.update(remoteUser.toRoomEntity().copy(localId = existing.localId))
            } else {
                usuarioDao.insert(remoteUser.toRoomEntity())
            }
        } catch (e: Exception) {
            // Error handling as per requirement: do not clear Room, behave as current application
        }
    }

    override suspend fun createUsuario(usuario: Usuario): Usuario {
        val created = usuarioApi.createUsuario(usuario)
        usuarioDao.insert(created.toRoomEntity())
        return created
    }

    override suspend fun updateUsuario(id: Long, usuario: Usuario): Usuario {
        val updated = usuarioApi.updateUsuario(id, usuario)
        val existing = usuarioDao.getByServerId(id)
        if (existing != null) {
            usuarioDao.update(updated.toRoomEntity().copy(localId = existing.localId))
        } else {
            usuarioDao.insert(updated.toRoomEntity())
        }
        return updated
    }

    override suspend fun deleteUsuario(id: Long) {
        usuarioApi.deleteUsuario(id)
        val existing = usuarioDao.getByServerId(id)
        if (existing != null) {
            usuarioDao.delete(existing)
        }
    }
}
