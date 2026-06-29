package ni.edu.uam.autotrak.data.repository

import kotlinx.coroutines.flow.Flow
import ni.edu.uam.autotrak.data.remote.model.RegistroCombustible

interface RegistroCombustibleRepository {
    fun observeByVehiculoId(vehiculoId: Long): Flow<List<RegistroCombustible>>
    suspend fun refreshByVehiculoId(vehiculoId: Long)
    suspend fun create(vehiculoId: Long, registro: RegistroCombustible): RegistroCombustible
    suspend fun update(id: Long, registro: RegistroCombustible): RegistroCombustible
    suspend fun delete(id: Long)
    suspend fun getRendimiento(vehiculoId: Long): Double
    suspend fun getTotalGastado(vehiculoId: Long): Double
}
