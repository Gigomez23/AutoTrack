package ni.edu.uam.autotrak.data.remote.api

import ni.edu.uam.autotrak.data.remote.model.RegistroProblema
import ni.edu.uam.autotrak.data.remote.model.sync.RegistroProblemaSyncDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface RegistroProblemaApi {
    @GET("api/v1/problemas/vehiculo/{vehiculoId}")
    suspend fun getRegistroProblemaByVehiculoId(@Path("vehiculoId") vehiculoId: Long): List<RegistroProblema>

    @GET("api/v1/problemas/vehiculo/{vehiculoId}/apto-circular")
    suspend fun getAptoCircularByVehiculoId(@Path("vehiculoId") vehiculoId: Long): List<RegistroProblema>

    @POST("api/v1/problemas/vehiculo/{vehiculoId}")
    suspend fun createRegistroProblema(@Path("vehiculoId") vehiculoId: Long, @Body registroProblema: RegistroProblema): RegistroProblema

    @PUT("api/v1/problemas/{id}")
    suspend fun updateRegistroProblema(@Path("id") id: Long, @Body registroProblema: RegistroProblema): RegistroProblema

    @PATCH("api/v1/problemas/{id}/solucionar")
    suspend fun solventarRegistroProblema(@Path("id") id: Long)

    @GET("api/v1/problemas/updated-after/{timestamp}")
    suspend fun getUpdatedAfter(@Path("timestamp") timestamp: Long): List<RegistroProblemaSyncDto>
}