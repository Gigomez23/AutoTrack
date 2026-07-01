package ni.edu.uam.autotrak.data.remote.api

import ni.edu.uam.autotrak.data.remote.model.ServicioMantenimiento
import ni.edu.uam.autotrak.data.remote.model.sync.ServicioMantenimientoSyncDTO
import retrofit2.http.*

interface ServicioMantenimientoApi {
    @GET("api/v1/servicios_mantenimiento")
    suspend fun getServiciosMantenimiento(): List<ServicioMantenimientoSyncDTO>

    @GET("api/v1/servicios_mantenimiento/{id}")
    suspend fun getServicioMantenimientoById(@Path("id") id: Long): ServicioMantenimientoSyncDTO

    @GET("api/v1/servicios_mantenimiento/vehiculos/{vehiculoId}/servicios_mantenimiento")
    suspend fun getServiciosByVehiculoId(@Path("vehiculoId") vehiculoId: Long): List<ServicioMantenimientoSyncDTO>

    @GET("api/v1/servicios_mantenimiento/updated-after/{timestamp}")
    suspend fun getUpdatedAfter(@Path("timestamp") timestamp: Long): List<ServicioMantenimientoSyncDTO>

    @POST("api/v1/servicios_mantenimiento")
    suspend fun createServicioMantenimiento(@Body servicio: ServicioMantenimiento): ServicioMantenimiento

    @PUT("api/v1/servicios_mantenimiento/{id}")
    suspend fun updateServicioMantenimiento(@Path("id") id: Long, @Body servicio: ServicioMantenimiento): ServicioMantenimiento

    @DELETE("api/v1/servicios_mantenimiento/{id}")
    suspend fun deleteServicioMantenimiento(@Path("id") id: Long)

    @PATCH("api/v1/servicios_mantenimiento/{id}/completado")
    suspend fun completarServicio(@Path("id") id: Long)
}
