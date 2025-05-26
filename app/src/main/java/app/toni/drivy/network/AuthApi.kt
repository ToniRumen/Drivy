package app.toni.drivy.network


import app.toni.drivy.network.models.car.CarResponse
import app.toni.drivy.network.models.car.CocheUpdateRequest
import app.toni.drivy.network.models.car.Gasolinera
import app.toni.drivy.network.models.user.LoginRequest
import app.toni.drivy.network.models.user.RegisterRequest
import app.toni.drivy.network.models.user.RutaRequest
import app.toni.drivy.network.models.user.RutaResponse
import app.toni.drivy.network.models.user.UserResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface AuthApi {

    @POST("auth/login")
    fun login(@Body request: LoginRequest): Call<ResponseBody>

    @POST("auth/register")
    fun register(@Body request: RegisterRequest): Call<ResponseBody>

    @GET("auth/me")
    fun getPerfil(
        @Header("Authorization") token: String
    ): Call<UserResponse>



    @GET("coches")
    fun getMisCoches(@Header("Authorization") token: String): Call<List<CarResponse>>

    @GET("coches/compartidos")
    fun getCochesComunidad(@Header("Authorization") token: String): Call<List<CarResponse>>

    @PUT("coches/{id}")
    fun actualizarCoche(
        @Path("id") id: Long,
        @Header("Authorization") token: String,
        @Body request: CocheUpdateRequest
    ): Call<CarResponse>


    @POST("/coches")
    fun crearCoche(@Header("Authorization") token: String, @Body coche: CocheUpdateRequest): Call<CarResponse>

    @DELETE("coches/{id}")
    fun eliminarCoche(@Path("id") id: Int, @Header("Authorization") token: String): Call<Void>




    @GET("rutas")
    fun getHistorialRutas(@Header("Authorization") token: String): Call<List<RutaResponse>>

    @POST("rutas")
    fun guardarRuta(
        @Header("Authorization") token: String,
        @Body request: RutaRequest
    ): Call<Void>

    @DELETE("rutas/{id}")
    fun eliminarRuta(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Call<Void>


    @GET("EstacionesTerrestres/")
    suspend fun getGasolineras(): Gasolinera


    @POST("admin/gasolineras/recargar")
    suspend fun recargarGasolineras(): Response<String>

}
