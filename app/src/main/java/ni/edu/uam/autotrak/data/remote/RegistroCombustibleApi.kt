package ni.edu.uam.autotrak.data.remote

import ni.edu.uam.autotrak.data.model.RegistroCombustible
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface RegistroCombustibleApi {
    @GET("api/registro-combustible")
    suspend fun getRegistrosCombustible(): List<RegistroCombustible>

    @GET("api/registro-combustible/{id}")
    suspend fun getRegistroCombustibleById(@Path("id") id: Long): RegistroCombustible

    @POST("api/registro-combustible")
    suspend fun createRegistroCombustible(@Body registroCombustible: RegistroCombustible): RegistroCombustible

    @PUT("api/registro-combustible/{id}")
    suspend fun updateRegistroCombustible(@Path("id") id: Long, @Body registroCombustible: RegistroCombustible): RegistroCombustible

    @DELETE("api/registro-combustible/{id}")
    suspend fun deleteRegistroCombustible(@Path("id") id: Long)
}