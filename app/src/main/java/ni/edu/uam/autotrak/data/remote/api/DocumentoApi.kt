package ni.edu.uam.autotrak.data.remote.api

import ni.edu.uam.autotrak.data.remote.model.Documento
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface DocumentoApi {
    @GET("api/documentos")
    suspend fun getDocumentos(): List<DocumentoApi>

    @GET("api/documentos/{id}")
    suspend fun getDocumentoById(@Path("id") id: Long): Documento

    @POST("api/documentos")
    suspend fun createDocumento(@Body documento: DocumentoApi): Documento

    @PUT("api/documentos/{id}")
    suspend fun updateDocumento(@Path("id") id: Long, @Body documento: Documento): Documento

    @DELETE("api/documentos/{id}")
    suspend fun deleteDocumento(@Path("id") id: Long)
}