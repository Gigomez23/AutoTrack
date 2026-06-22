package ni.edu.uam.autotrak.data.remote.api

import ni.edu.uam.autotrak.data.remote.model.DocumentoLicencia
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface DocumentoLicenciaApi {
    @GET("api/documentosLicencia")
//    suspend fun getDocumentosLicencia(): DocumentoLicencia
    suspend fun getDocumentosLicencia(): List<DocumentoLicencia>

    @GET("api/documentosLicencia/{id}")
    suspend fun getDocumentoLicenciaById(@Path("id") id: Long): DocumentoLicencia

    @POST("api/documentosLicencia")
    suspend fun createDocumentoLicencia(@Body documentoLicencia: DocumentoLicencia): DocumentoLicencia

    @PUT("api/documentosLicencia/{id}")
    suspend fun updateDocumentoLicencia(@Path("id") id: Long, @Body documentoLicencia: DocumentoLicencia): DocumentoLicencia

    @DELETE("api/documentosLicencia/{id}")
    suspend fun deleteDocumentoLicencia(@Path("id") id: Long)
}