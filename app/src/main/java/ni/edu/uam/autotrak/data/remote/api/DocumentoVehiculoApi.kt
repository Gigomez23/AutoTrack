package ni.edu.uam.autotrak.data.remote.api

import ni.edu.uam.autotrak.data.remote.model.DocumentoVehiculo
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface DocumentoVehiculoApi {
    @GET("api/documentosVehiculo")
    suspend fun getDocumentosVehiculo(): List<DocumentoVehiculo>

    @GET("api/documentosVehiculo/{id}")
    suspend fun getDocumentoVehiculoById(@Path("id") id: Long): DocumentoVehiculo

    @POST("api/documentosVehiculo")
    suspend fun createDocumentoVehiculo(@Body documentoVehiculo: DocumentoVehiculo): DocumentoVehiculo

    @PUT("api/documentosVehiculo/{id}")
    suspend fun updateDocumentoVehiculo(@Path("id") id: Long, @Body documentoVehiculo: DocumentoVehiculo): DocumentoVehiculo

    @DELETE("api/documentosVehiculo/{id}")
    suspend fun deleteDocumentoVehiculo(@Path("id") id: Long)
}