package ni.edu.uam.autotrak.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface RegistroProblemaApi {
    @GET("api/registro-problema")
    suspend fun getRegistrosProblema(): List<RegistroProblemaApi>

    @GET("api/registro-problema/{id}")
    suspend fun getRegistroProblemaById(@Path("id") id: Long): RegistroProblemaApi

    @POST("api/registro-problema")
    suspend fun createRegistroProblema(@Body registroProblema: RegistroProblemaApi): RegistroProblemaApi

    @PUT("api/registro-problema/{id}")
    suspend fun updateRegistroProblema(@Path("id") id: Long, @Body registroProblema: RegistroProblemaApi): RegistroProblemaApi
}