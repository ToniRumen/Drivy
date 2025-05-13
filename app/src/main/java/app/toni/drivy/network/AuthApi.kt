package app.toni.drivy.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Header

interface AuthApi {

    @POST("auth/login")
    fun login(@Body request: LoginRequest): Call<ResponseBody>

    @POST("auth/register")
    fun register(@Body request: RegisterRequest): Call<ResponseBody>

    @GET("auth/me")
    fun getPerfil(
        @Header("Authorization") token: String
    ): Call<UserResponse>
}
