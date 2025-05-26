package app.toni.drivy.network.models

import app.toni.drivy.network.models.car.EstacionServicio
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface PrecioilApi {
    @GET("estaciones/radio")
    suspend fun obtenerEstacionesCercanas(
        @Query("latitud") latitud: Double,
        @Query("longitud") longitud: Double,
        @Query("radio") radio: Int,
        @Query("pagina") pagina: Int,
        @Query("limite") limite: Int
    ): Response<List<EstacionServicio>>
}
