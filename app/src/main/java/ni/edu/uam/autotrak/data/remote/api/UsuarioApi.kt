package ni.edu.uam.autotrak.data.remote.api

import ni.edu.uam.autotrak.data.remote.model.LoginRequest
import ni.edu.uam.autotrak.data.remote.model.LoginResponse
import ni.edu.uam.autotrak.data.remote.model.Usuario
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface UsuarioApi {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("api/v1/usuarios/{id}")
    suspend fun getUsuario(@Path("id") id: Long): Usuario

    @POST("api/v1/usuarios")
    suspend fun createUsuario(@Body usuario: Usuario): Usuario

    @PUT("api/v1/usuarios/{id}")
    suspend fun updateUsuario(@Path("id") id: Long, @Body usuario: Usuario): Usuario

    @DELETE("api/v1/usuarios/{id}")
    suspend fun deleteUsuario(@Path("id") id: Long)
}