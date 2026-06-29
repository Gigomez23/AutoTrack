package ni.edu.uam.autotrak.data.remote.api

import ni.edu.uam.autotrak.data.remote.model.RegistroCombustible
import ni.edu.uam.autotrak.data.remote.model.sync.RegistroCombustibleSyncDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface RegistroCombustibleApi {
    @POST("api/v1/combustibles/vehiculo/{vehiculoId}")
    suspend fun createRegistroCombustible(@Path("vehiculoId") vehiculoId: Long,
                                          @Body registroCombustible: RegistroCombustible): RegistroCombustible

    @GET("api/v1/combustibles/vehiculo/{vehiculoId}")
    suspend fun getRegistroCombustibleByVehiculoId(@Path("vehiculoId") vehiculoId: Long): List<RegistroCombustible>

    @GET("api/v1/combustibles/vehiculo/{vehiculoId}/total-gastado")
    suspend fun getTotalGastadoByVehiculoId(@Path("vehiculoId") vehiculoId: Long): Double

    @GET("api/v1/combustibles/vehiculo/{vehiculoId}/rendimiento")
    suspend fun getRendimientoByVehiculoId(@Path("vehiculoId") vehiculoId: Long): Double

    @GET("api/v1/combustibles/updated-after/{timestamp}")
    suspend fun getUpdatedAfter(@Path("timestamp") timestamp: Long): List<RegistroCombustibleSyncDto>
}