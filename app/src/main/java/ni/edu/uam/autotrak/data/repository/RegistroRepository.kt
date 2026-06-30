package ni.edu.uam.autotrak.data.repository

import kotlinx.coroutines.flow.Flow
import ni.edu.uam.autotrak.data.remote.model.RegistroGeneral

interface RegistroRepository {
    fun observeAll(): Flow<List<RegistroGeneral>>
    suspend fun refreshAll()
    fun observeByVehiculoId(vehiculoId: Long): Flow<List<RegistroGeneral>>
    suspend fun refreshByVehiculoId(vehiculoId: Long)
    suspend fun delete(id: Long)
}
