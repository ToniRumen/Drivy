import app.toni.drivy.network.models.car.Gasolinera
import retrofit2.http.GET
import retrofit2.http.Query

interface GasolineraApi {
    @GET("api/gasolineras/cercanas")
    suspend fun getGasolinerasCercanas(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("limite") limite: Int = 10
    ): List<Gasolinera>
}
