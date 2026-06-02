package ni.edu.uam.autotrak.data.remote

import ni.edu.uam.autotrak.data.model.Usuario
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface UsuarioApi {
    @GET("api/usuarios/{id}")
    suspend fun getUsuario(@Path("id") id: Long): Usuario

    @POST("api/usuarios")
    suspend fun createUsuario(@Body usuario: Usuario): Usuario

    @PUT("api/usuarios/{id}")
    suspend fun updateUsuario(@Path("id") id: Long, @Body usuario: Usuario): Usuario

    @DELETE("api/usuarios/{id}")
    suspend fun deleteUsuario(@Path("id") id: Long)
}