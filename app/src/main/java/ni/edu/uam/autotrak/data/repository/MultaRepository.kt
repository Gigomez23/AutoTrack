package ni.edu.uam.autotrak.data.repository

import kotlinx.coroutines.flow.Flow
import ni.edu.uam.autotrak.data.remote.model.Multa

interface MultaRepository {
    fun observeByUsuarioId(usuarioId: Long): Flow<List<Multa>>
    suspend fun refreshByUsuarioId(usuarioId: Long)
    suspend fun create(multa: Multa): Multa
    suspend fun update(id: Long, multa: Multa): Multa
    suspend fun pagar(id: Long): Multa
    suspend fun delete(id: Long)
}
