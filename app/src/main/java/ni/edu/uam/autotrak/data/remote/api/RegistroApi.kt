package ni.edu.uam.autotrak.data.remote.api

import ni.edu.uam.autotrak.data.remote.model.Registro
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface RegistroApi {
    @GET("api/registros")
    suspend fun getRegistros(): List<Registro>

    @GET("api/registros/{id}")
    suspend fun getRegistroById(@Path("id") id: Long): Registro

    @POST("api/registros")
    suspend fun createRegistro(@Body registro: Registro): Registro

    @PUT("api/registros/{id}")
    suspend fun updateRegistro(@Path("id") id: Long, @Body registro: Registro): Registro

    @DELETE("api/registros/{id}")
    suspend fun deleteRegistro(@Path("id") id: Long)
}