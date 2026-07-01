package ni.edu.uam.autotrak.data.repository

import kotlinx.coroutines.flow.Flow
import ni.edu.uam.autotrak.data.remote.model.DocumentoVehiculo

interface DocumentoVehiculoRepository {
    fun observeByVehiculoId(vehiculoId: Long): Flow<List<DocumentoVehiculo>>
    suspend fun refreshByVehiculoId(vehiculoId: Long)
    suspend fun create(vehiculoId: Long, documentoVehiculo: DocumentoVehiculo): DocumentoVehiculo
    suspend fun update(id: Long, documentoVehiculo: DocumentoVehiculo): DocumentoVehiculo
    suspend fun delete(id: Long)
}
