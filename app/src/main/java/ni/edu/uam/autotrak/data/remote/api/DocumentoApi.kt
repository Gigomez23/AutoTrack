package ni.edu.uam.autotrak.data.remote.api

import ni.edu.uam.autotrak.data.remote.model.Documento
import ni.edu.uam.autotrak.data.remote.model.sync.DocumentoSyncDTO
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.PUT
import retrofit2.http.Path

interface DocumentoApi {
    @GET("api/v1/documentos")
    suspend fun getDocumentos(): List<Documento>

    @GET("api/v1/documentos/{id}")
    suspend fun getDocumentoById(@Path("id") id: Long): Documento

    @GET("api/v1/documentos/vencidos")
    suspend fun getDocumentosVencidos(): List<Documento>

    @GET("api/v1/documentos/updated-after/{timestamp}")
    suspend fun getUpdatedAfter(@Path("timestamp") timestamp: Long): List<DocumentoSyncDTO>

    @PUT("api/v1/documentos")
    suspend fun updateDocumento(@Body documento: Documento): Documento

    @HTTP(method = "DELETE", path = "api/v1/documentos", hasBody = true)
    suspend fun deleteDocumento(@Body documento: DocumentoSyncDTO)
}
