package ni.edu.uam.autotrak.data.remote

import ni.edu.uam.autotrak.data.remote.api.DocumentoApi
import ni.edu.uam.autotrak.data.remote.api.DocumentoLicenciaApi
import ni.edu.uam.autotrak.data.remote.api.DocumentoVehiculoApi
import ni.edu.uam.autotrak.data.remote.api.PreferenciasApi
import ni.edu.uam.autotrak.data.remote.api.RegistroApi
import ni.edu.uam.autotrak.data.remote.api.RegistroCombustibleApi
import ni.edu.uam.autotrak.data.remote.api.RegistroProblemaApi
import ni.edu.uam.autotrak.data.remote.api.UsuarioApi
import ni.edu.uam.autotrak.data.remote.api.VehiculoApi
import com.google.gson.GsonBuilder
import java.math.BigDecimal
import java.time.LocalDate
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    //todo: change this to the actual ip address of the machine running the backend server
//    private const val BASE_URL = "http://10.0.2.2:8080/"
    private const val BASE_URL = "http://127.0.0.1:8080/"

    private var sessionManager: SessionManager? = null

    fun init(sessionManager: SessionManager) {
        this.sessionManager = sessionManager
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                sessionManager?.getAuthHeader()?.let {
                    requestBuilder.addHeader("Authorization", it)
                }
                chain.proceed(requestBuilder.build())
            }
            .build()
    }

    private val gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
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
}