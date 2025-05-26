package app.toni.drivy.network

import GasolineraApi
import app.toni.drivy.network.models.PrecioilApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitClient {

    private const val BASE_URL = "http://192.168.18.60:8080/"
    private const val PRECIOIL_BASE_URL = "https://api.precioil.es/"

    //Ordenador Torre: 192.168.18.60


    // Instancia para tu backend
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val authApi: AuthApi by lazy { retrofit.create(AuthApi::class.java) }
    val gasolineraApi: GasolineraApi by lazy { retrofit.create(GasolineraApi::class.java) }

    // Instancia para la API de Precioil
    private val retrofitPrecioil: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(PRECIOIL_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val precioilApi: PrecioilApi by lazy { retrofitPrecioil.create(PrecioilApi::class.java) }
}