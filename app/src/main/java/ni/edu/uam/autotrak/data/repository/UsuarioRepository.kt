package ni.edu.uam.autotrak.data.repository

import kotlinx.coroutines.flow.Flow
import ni.edu.uam.autotrak.data.remote.model.LoginRequest
import ni.edu.uam.autotrak.data.remote.model.LoginResponse
import ni.edu.uam.autotrak.data.remote.model.Usuario

interface UsuarioRepository {
    suspend fun login(request: LoginRequest): LoginResponse
    fun observeUsuario(id: Long): Flow<Usuario?>
    suspend fun refreshUsuario(id: Long)
    suspend fun createUsuario(usuario: Usuario): Usuario
    suspend fun updateUsuario(id: Long, usuario: Usuario): Usuario
    suspend fun deleteUsuario(id: Long)
}
