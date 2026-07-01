package ni.edu.uam.autotrak.data.repository

import kotlinx.coroutines.flow.Flow
import ni.edu.uam.autotrak.data.remote.model.Licencia

interface LicenciaRepository {
    fun observeByUsuarioId(usuarioId: Long): Flow<List<Licencia>>
    suspend fun refreshByUsuarioId(usuarioId: Long)
    suspend fun delete(id: Long)
    suspend fun create(licencia: Licencia): Licencia
    suspend fun update(id: Long, licencia: Licencia): Licencia
}