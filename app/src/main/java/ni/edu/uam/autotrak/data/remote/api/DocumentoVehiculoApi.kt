package ni.edu.uam.autotrak.data.remote.api

import ni.edu.uam.autotrak.data.remote.model.DocumentoVehiculo
import ni.edu.uam.autotrak.data.remote.model.sync.DocumentoVehiculoSyncDTO
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface DocumentoVehiculoApi {
    @GET("api/v1/documentos-vehiculos")
    suspend fun getDocumentosVehiculo(): List<DocumentoVehiculo>

    @GET("api/v1/documentos-vehiculos/{id}")
    suspend fun getDocumentoVehiculoById(@Path("id") id: Long): DocumentoVehiculo

    @GET("api/v1/documentos-vehiculos/vehiculo/{vehiculoId}")
    suspend fun getDocumentosVehiculoByVehiculoId(@Path("vehiculoId") vehiculoId: Long): List<DocumentoVehiculo>

    @GET("api/v1/documentos-vehiculos/updated-after/{timestamp}")
    suspend fun getUpdatedAfter(@Path("timestamp") timestamp: Long): List<DocumentoVehiculoSyncDTO>

    @POST("api/v1/documentos-vehiculos")
    suspend fun createDocumentoVehiculo(@Body documentoVehiculo: DocumentoVehiculo): DocumentoVehiculo

    @PUT("api/v1/documentos-vehiculos/{id}")
    suspend fun updateDocumentoVehiculo(@Path("id") id: Long, @Body documentoVehiculo: DocumentoVehiculo): DocumentoVehiculo

    @DELETE("api/v1/documentos-vehiculos/{id}")
    suspend fun deleteDocumentoVehiculo(@Path("id") id: Long)
}
