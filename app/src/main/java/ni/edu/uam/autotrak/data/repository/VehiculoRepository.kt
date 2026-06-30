package ni.edu.uam.autotrak.data.repository

import kotlinx.coroutines.flow.Flow
import ni.edu.uam.autotrak.data.remote.model.Vehiculo

interface VehiculoRepository {
    fun observeVehiculos(usuarioId: Long): Flow<List<Vehiculo>>
    suspend fun refreshVehiculos(usuarioId: Long)
    fun observeVehiculoById(id: Long): Flow<Vehiculo?>
    suspend fun refreshVehiculoById(id: Long)
    suspend fun createVehiculo(vehiculo: Vehiculo): Vehiculo
    suspend fun updateVehiculo(id: Long, vehiculo: Vehiculo): Vehiculo
    suspend fun deleteVehiculo(id: Long)
}
