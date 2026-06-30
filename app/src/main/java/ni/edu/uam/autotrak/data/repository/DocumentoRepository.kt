package ni.edu.uam.autotrak.data.repository

import kotlinx.coroutines.flow.Flow
import ni.edu.uam.autotrak.data.remote.model.DocumentoGeneral

interface DocumentoRepository {
    fun observeAll(): Flow<List<DocumentoGeneral>>
    suspend fun refreshAll()
    suspend fun delete(id: Long)
}