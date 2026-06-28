package ni.edu.uam.autotrak.data.remote.api

import ni.edu.uam.autotrak.data.remote.model.Registro
import ni.edu.uam.autotrak.data.remote.model.RegistroGeneral
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface RegistroApi {
    @GET("api/v1/registros")
    suspend fun getRegistros(): List<RegistroGeneral>

    @GET("api/v1/registros/{id}")
    suspend fun getRegistroById(@Path("id") id: Long): RegistroGeneral

    @GET("api/v1/registros/vehiculo/{vehiculoId}/filtrar")
    suspend fun getRegistrosByVehiculoId(@Path("vehiculoId") vehiculoId: Long): List<RegistroGeneral>

    @GET("api/v1/registros/vehiculo/{vehiculoId}/historial")
    suspend fun getHistorialByVehiculoId(@Path("vehiculoId") vehiculoId: Long): List<RegistroGeneral>

    @DELETE("api/v1/registros/{id}")
    suspend fun deleteRegistro(@Path("id") id: Long)
}
