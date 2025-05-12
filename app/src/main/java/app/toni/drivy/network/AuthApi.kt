package app.toni.drivy.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import okhttp3.ResponseBody

interface AuthApi {

    @POST("auth/login")
    fun login(@Body request: LoginRequest): Call<ResponseBody>

    @POST("auth/register")
    fun register(@Body request: RegisterRequest): Call<ResponseBody>

}
