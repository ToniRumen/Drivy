package app.toni.drivy.fragments.cargaServer


import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

object ServerChecker {

    fun checkServerIsUp(onResult: (Boolean) -> Unit) {
        val client = OkHttpClient.Builder()
            .callTimeout(5, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder()
            .url("https://api-drivy.onrender.com")
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                onResult(false)
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val isUp = response.code != 0 && response.code < 500
                onResult(isUp)
                response.close()
            }
        })
    }
}
