package ni.edu.uam.autotrak.data.remote.api

import ni.edu.uam.autotrak.data.remote.model.Licencia
import ni.edu.uam.autotrak.data.remote.model.sync.LicenciaSyncDTO
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface LicenciaApi {
    @GET("api/v1/licencias")
    suspend fun getLicencias(): List<Licencia>

    @GET("api/v1/licencias/{id}")
    suspend fun getLicenciaById(@Path("id") id: Long): Licencia

    @GET("api/v1/licencias/usuario/{usuarioId}")
    suspend fun getLicenciaByUsuarioId(@Path("usuarioId") usuarioId: Long): Licencia

    @GET("api/v1/licencias/updated-after/{timestamp}")
    suspend fun getUpdatedAfter(@Path("timestamp") timestamp: Long): List<LicenciaSyncDTO>

    @POST("api/v1/licencias")
    suspend fun createLicencia(@Body licencia: Licencia): Licencia

    @PUT("api/v1/licencias/{id}")
    suspend fun updateLicencia(@Path("id") id: Long, @Body licencia: Licencia): Licencia

    @DELETE("api/v1/licencias/{id}")
    suspend fun deleteLicencia(@Path("id") id: Long)
}
