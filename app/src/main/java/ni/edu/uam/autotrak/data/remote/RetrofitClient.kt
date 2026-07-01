package ni.edu.uam.autotrak.data.remote

import ni.edu.uam.autotrak.data.remote.api.DocumentoApi
import ni.edu.uam.autotrak.data.remote.api.LicenciaApi
import ni.edu.uam.autotrak.data.remote.api.MultaApi
import ni.edu.uam.autotrak.data.remote.api.RegistroApi
import ni.edu.uam.autotrak.data.remote.api.RegistroCombustibleApi
import ni.edu.uam.autotrak.data.remote.api.RegistroProblemaApi
import ni.edu.uam.autotrak.data.remote.api.UsuarioApi
import ni.edu.uam.autotrak.data.remote.api.VehiculoApi
import com.google.gson.GsonBuilder
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.io.IOException
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    //todo: change this to the actual ip address of the machine running the backend server
//    private const val BASE_URL = "http://10.0.2.2:8080/"
    private const val BASE_URL = "http://127.0.0.1:8080/"

    private var sessionManager: SessionManager? = null
    private var serverStatusMonitor: ServerStatusMonitor? = null

    fun init(sessionManager: SessionManager, serverStatusMonitor: ServerStatusMonitor) {
        this.sessionManager = sessionManager
        this.serverStatusMonitor = serverStatusMonitor
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                sessionManager?.getAuthHeader()?.let {
                    requestBuilder.addHeader("Authorization", it)
                }
                val request = requestBuilder.build()
                try {
                    val response = chain.proceed(request)
                    if (response.isSuccessful) {
                        serverStatusMonitor?.reportSuccess()
                    } else if (response.code >= 500) {
                        serverStatusMonitor?.reportError(IOException("Server error: ${response.code}"))
                    }
                    response
                } catch (e: Exception) {
                    serverStatusMonitor?.reportError(e)
                    throw e
                }
            }
            .build()
    }

    val gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
        .registerTypeAdapter(BigDecimal::class.java, BigDecimalAdapter())
        .create()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val api_usuario: UsuarioApi by lazy {
        retrofit.create(UsuarioApi::class.java)
    }

    val api_vehiculo: VehiculoApi by lazy {
        retrofit.create(VehiculoApi::class.java)
    }

    val api_registro: RegistroApi by lazy {
        retrofit.create(RegistroApi::class.java)
    }

    val api_registro_problema: RegistroProblemaApi by lazy {
        retrofit.create(RegistroProblemaApi::class.java)
    }

    val api_registro_combustible: RegistroCombustibleApi by lazy {
        retrofit.create(RegistroCombustibleApi::class.java)
    }

    val api_documento: DocumentoApi by lazy {
        retrofit.create(DocumentoApi::class.java)
    }

    val api_licencia: LicenciaApi by lazy {
        retrofit.create(LicenciaApi::class.java)
    }

    val api_multa: MultaApi by lazy {
        retrofit.create(MultaApi::class.java)
    }
}
