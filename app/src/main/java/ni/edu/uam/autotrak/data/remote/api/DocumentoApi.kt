package ni.edu.uam.autotrak.data.remote.api

import ni.edu.uam.autotrak.data.remote.model.Documento
import ni.edu.uam.autotrak.data.remote.model.DocumentoGeneral
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface DocumentoApi {
    @GET("api/v1/documentos")
    suspend fun getDocumentos(): List<DocumentoGeneral>

    @GET("api/v1/documentos/{id}")
    suspend fun getDocumentoById(@Path("id") id: Long): DocumentoGeneral

    @GET("api/v1/documentos/vencidos")
    suspend fun getDocumentosVencidos(): List<DocumentoGeneral>

    @PUT("api/v1/documentos/{id}")
    suspend fun updateDocumento(@Path("id") id: Long, @Body documento: DocumentoGeneral): DocumentoGeneral

    @DELETE("api/v1/documentos/{id}")
    suspend fun deleteDocumento(@Path("id") id: Long)
}
