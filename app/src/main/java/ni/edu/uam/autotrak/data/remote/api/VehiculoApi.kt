package ni.edu.uam.autotrak.data.remote.api

import ni.edu.uam.autotrak.data.remote.model.Vehiculo
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface VehiculoApi {
    @GET("api/vehiculos")
    suspend fun getVehiculos(): List<Vehiculo>

    @GET("api/vehiculos/{id}")
    suspend fun getVehiculoById(@Path("id") id: Long): Vehiculo

    @POST("api/vehiculos")
    suspend fun createVehiculo(@Body vehiculo: Vehiculo): Vehiculo

    @PUT("api/vehiculos/{id}")
    suspend fun updateVehiculo(@Path("id") id: Long, @Body vehiculo: Vehiculo): Vehiculo

    @DELETE("api/vehiculos/{id}")
    suspend fun deleteVehiculo(@Path("id") id: Long)
}