package app.toni.drivy.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://172.20.10.3:8080/"

    /*
    IP de torre: "http://192.168.18.60:8080/"
    IP del portatil : "http://172.20.10.3:8080/"
    */

    val instance: AuthApi by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(AuthApi::class.java)
    }
}
