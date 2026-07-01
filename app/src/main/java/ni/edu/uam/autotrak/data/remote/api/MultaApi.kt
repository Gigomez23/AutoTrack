package ni.edu.uam.autotrak.data.remote.api

import ni.edu.uam.autotrak.data.remote.model.Multa
import ni.edu.uam.autotrak.data.remote.model.sync.MultaSyncDTO
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface MultaApi {
    @GET("api/v1/multas")
    suspend fun getMultas(): List<Multa>

    @GET("api/v1/multas/{id}")
    suspend fun getMultaById(@Path("id") id: Long): Multa

    @GET("api/v1/multas/usuario/{usuarioId}")
    suspend fun getMultasByUsuarioId(@Path("usuarioId") usuarioId: Long): List<Multa>

    @GET("api/v1/multas/updated-after/{timestamp}")
    suspend fun getUpdatedAfter(@Path("timestamp") timestamp: Long): List<MultaSyncDTO>

    @POST("api/v1/multas")
    suspend fun createMulta(@Body multa: Multa): Multa

    @PUT("api/v1/multas/{id}")
    suspend fun updateMulta(@Path("id") id: Long, @Body multa: Multa): Multa

    @PATCH("api/v1/multas/{id}/pagar")
    suspend fun pagarMulta(@Path("id") id: Long)

    @DELETE("api/v1/multas/{id}")
    suspend fun deleteMulta(@Path("id") id: Long)
}
