package ni.edu.uam.autotrak.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    //todo: change this to the actual ip address of the machine running the backend server
    private const val BASE_URL = "http:/10.0.2.2:8080/"

    //todo: this might be eager later on...
    val api_usuario: UsuarioApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UsuarioApi::class.java)
    }

    val api_vehiculo: VehiculoApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(VehiculoApi::class.java)
    }

    val api_preferencia: PreferenciasApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PreferenciasApi::class.java)
    }

    val api_registro: RegistroApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RegistroApi::class.java)
    }

    val api_registro_problema: RegistroProblemaApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RegistroProblemaApi::class.java)
    }

    val api_registro_combustible: RegistroCombustibleApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RegistroCombustibleApi::class.java)
    }

    val api_documento: DocumentoApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DocumentoApi::class.java)
    }

    val api_documento_vehiculo: DocumentoVehiculoApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DocumentoVehiculoApi::class.java)
    }

    val api_documento_licencia: DocumentoLicenciaApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DocumentoLicenciaApi::class.java)
    }


}