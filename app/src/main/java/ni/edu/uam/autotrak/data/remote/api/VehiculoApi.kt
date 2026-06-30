package ni.edu.uam.autotrak.data.remote.api

import ni.edu.uam.autotrak.data.remote.model.Vehiculo
import ni.edu.uam.autotrak.data.remote.model.sync.VehiculoSyncDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface VehiculoApi {
    @GET("api/v1/vehiculos")
    suspend fun getVehiculos(): List<Vehiculo>

    @GET("api/v1/vehiculos/{id}")
    suspend fun getVehiculoById(@Path("id") id: Long): Vehiculo

    @GET("api/v1/vehiculos/usuario/{usuarioId}")
    suspend fun getVehiculoByUsuarioId(@Path("usuarioId") usuarioId: Long): List<Vehiculo>

    @GET("api/v1/vehiculos/buscar-por-placa/{placa}")
    suspend fun getVehiculoByPlaca(@Path("placa") placa: String): Vehiculo

    @GET("api/v1/vehiculos/buscar-por-vin/{vin}")
    suspend fun getVehiculoByVin(@Path("vin") vin: String): Vehiculo

    @GET("api/v1/vehiculos/updated-after/{timestamp}")
    suspend fun getUpdatedAfter(@Path("timestamp") timestamp: Long): List<VehiculoSyncDto>

    @POST("api/v1/vehiculos")
    suspend fun createVehiculo(@Body vehiculo: Vehiculo): Vehiculo

    @PUT("api/v1/vehiculos/{id}")
    suspend fun updateVehiculo(@Path("id") id: Long, @Body vehiculo: Vehiculo): Vehiculo

    @DELETE("api/v1/vehiculos/{id}")
    suspend fun deleteVehiculo(@Path("id") id: Long)
}