package ni.edu.uam.autotrak.data.repository

import kotlinx.coroutines.flow.Flow
import ni.edu.uam.autotrak.data.remote.model.Documento

interface DocumentoRepository {
    fun observeAll(): Flow<List<Documento>>
    suspend fun refreshAll()
    suspend fun create(documento: Documento): Documento
    suspend fun update(id: Long, documento: Documento): Documento
    suspend fun delete(id: Long)
}
