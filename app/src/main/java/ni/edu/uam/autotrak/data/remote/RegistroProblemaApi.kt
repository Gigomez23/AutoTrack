package ni.edu.uam.autotrak.data.remote

import ni.edu.uam.autotrak.data.model.RegistroProblema
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface RegistroProblemaApi {
    @GET("api/registro-problema")
    suspend fun getRegistrosProblema(): List<RegistroProblema>

    @GET("api/registro-problema/{id}")
    suspend fun getRegistroProblemaById(@Path("id") id: Long): RegistroProblema

    @POST("api/registro-problema")
    suspend fun createRegistroProblema(@Body registroProblema: RegistroProblema): RegistroProblema

    @PUT("api/registro-problema/{id}")
    suspend fun updateRegistroProblema(@Path("id") id: Long, @Body registroProblema: RegistroProblema): RegistroProblema
}