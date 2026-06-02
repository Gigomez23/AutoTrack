package ni.edu.uam.autotrak.data.remote

import ni.edu.uam.autotrak.data.model.Prefrenecias
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface PreferenciasApi {
    @GET("api/preferencias")
    suspend fun getPreferencias(): Prefrenecias

    @POST("api/preferencias")
    suspend fun createPreferencias(@Body preferencias: Prefrenecias): Prefrenecias

    @PUT("api/preferencias/{id}")
    suspend fun updatePreferencias(@Path("id") id: Long, @Body preferencias: Prefrenecias): Prefrenecias

    @DELETE("api/preferencias/{id}")
    suspend fun deletePreferencias(@Path("id") id: Long)

}