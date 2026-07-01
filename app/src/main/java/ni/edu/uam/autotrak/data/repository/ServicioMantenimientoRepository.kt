package ni.edu.uam.autotrak.data.repository

import kotlinx.coroutines.flow.Flow
import ni.edu.uam.autotrak.data.remote.model.ServicioMantenimiento

interface ServicioMantenimientoRepository {
    fun observeByVehiculoId(vehiculoId: Long): Flow<List<ServicioMantenimiento>>
    suspend fun refreshByVehiculoId(vehiculoId: Long)
    suspend fun create(vehiculoId: Long, servicio: ServicioMantenimiento): ServicioMantenimiento
    suspend fun update(id: Long, servicio: ServicioMantenimiento): ServicioMantenimiento
    suspend fun delete(id: Long)
    suspend fun completarServicio(id: Long)
}
