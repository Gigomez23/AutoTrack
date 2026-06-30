package ni.edu.uam.autotrak.data.repository

import kotlinx.coroutines.flow.Flow
import ni.edu.uam.autotrak.data.remote.model.RegistroProblema

interface RegistroProblemaRepository {
    fun observeByVehiculoId(vehiculoId: Long): Flow<List<RegistroProblema>>
    suspend fun refreshByVehiculoId(vehiculoId: Long)
    suspend fun create(vehiculoId: Long, registro: RegistroProblema): RegistroProblema
    suspend fun update(id: Long, registro: RegistroProblema): RegistroProblema
    suspend fun delete(id: Long)
}
