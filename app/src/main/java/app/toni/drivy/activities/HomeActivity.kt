package app.toni.drivy.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import app.toni.drivy.R
import app.toni.drivy.fragments.coches.CochesTabFragment
import app.toni.drivy.fragments.menu.InicioFragment
import app.toni.drivy.fragments.menu.RutasFragment
import app.toni.drivy.network.RetrofitClient
import app.toni.drivy.network.models.user.UserResponse
import com.google.android.gms.location.LocationServices
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.abs

class HomeActivity : AppCompatActivity() {

    private lateinit var switcher: TextSwitcher
    private lateinit var fabInicio: FloatingActionButton
    private lateinit var fabRutas: FloatingActionButton
    private lateinit var fabCoches: FloatingActionButton
    private lateinit var viewPager: ViewPager2

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        switcher = findViewById(R.id.textSwitcherBienvenida)
        fabInicio = findViewById(R.id.fabInicio)
        fabRutas = findViewById(R.id.fabRutas)
        fabCoches = findViewById(R.id.fabCoches)
        viewPager = findViewById(R.id.viewPager)

        configurarTextSwitcher()
        cargarPerfilUsuario()

        // ViewPager
        viewPager.adapter = ScreenSlidePagerAdapter(this)
        viewPager.currentItem = 1
        viewPager.setPageTransformer(FadeSlidePageTransformer())
        actualizarFABs(1)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                actualizarFABs(position)
            }
        })


        fabInicio.setOnClickListener {
            animacionBoton(fabInicio)
                viewPager.currentItem = 1
        }

        fabRutas.setOnClickListener {
            animacionBoton(fabRutas)
            viewPager.currentItem = 0

        }

        fabCoches.setOnClickListener {
            animacionBoton(fabCoches)
                viewPager.currentItem = 2

        }




        // Drawer
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        val navView = findViewById<NavigationView>(R.id.navigationView)
        val btnHamburguesa = findViewById<ImageButton>(R.id.btnHamburguesa)

        btnHamburguesa.setOnClickListener {
            drawerLayout.openDrawer(android.view.Gravity.START)
        }

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_perfil -> Toast.makeText(this, "Perfil no disponible aún", Toast.LENGTH_SHORT).show()
                R.id.nav_historial -> viewPager.currentItem = 0
                R.id.nav_ajustes -> Toast.makeText(this, "Aquí irán los ajustes", Toast.LENGTH_SHORT).show()
                R.id.nav_logout -> {
                    getSharedPreferences("app", MODE_PRIVATE).edit().remove("jwt").apply()
                    startActivity(Intent(this, AuthActivity::class.java))
                    finish()
                }
            }
            drawerLayout.closeDrawers()
            true
        }

        // Tip del día abajo
        val tipText = findViewById<TextView>(R.id.textoTipDelDia)
        val tips = listOf(
            "💡 Usa modo Eco para ahorrar gasolina.",
            "🛞 Verifica la presión de los neumáticos regularmente.",
            "⛽ Combustible caro? Planifica rutas más cortas.",
            "🌿 Conduce suave: consume menos y contamina menos.",
            "🧠 Mantén una velocidad constante, evita frenazos."
        )
        tipText.text = tips.random()

        // Clima abajo
        cargarClimaActual()
    }


    class FadeSlidePageTransformer : ViewPager2.PageTransformer {
        override fun transformPage(page: View, position: Float) {
            page.apply {
                when {
                    position < -1 -> { // Página fuera de la izquierda
                        alpha = 0f
                    }
                    position <= 1 -> { // [-1,1]
                        alpha = 1 - abs(position)
                        translationX = -position * page.width * 0.3f
                        scaleY = 0.95f + (1 - abs(position)) * 0.05f
                    }
                    else -> { // Página fuera de la derecha
                        alpha = 0f
                    }
                }
            }
        }
    }

    private fun cargarClimaActual() {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
            return
        }

        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                Log.d("CLIMA", "Lat: ${location.latitude}, Lon: ${location.longitude}")
                obtenerClima(location.latitude, location.longitude)
            } else {
                Log.e("CLIMA", "Ubicación es null")
            }
        }
    }

    private fun obtenerClima(lat: Double, lon: Double) {
        val apiKey = "a84eff9d37fbd02f031d5a8825ef959c"
        val url = "https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&units=metric&lang=es&appid=$apiKey"

        Thread {
            try {
                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                val body = response.body?.string()

                if (!body.isNullOrEmpty()) {
                    val json = JSONObject(body)
                    val temp = json.getJSONObject("main").getDouble("temp").toInt()
                    val descripcion = json.getJSONArray("weather").getJSONObject(0).getString("description")

                    Log.d("CLIMA", "Clima recibido: $temp°C, $descripcion")

                    runOnUiThread {
                        val texto = findViewById<TextView>(R.id.textoClima)
                        val icono = findViewById<ImageView>(R.id.iconoClima)

                        texto.text = "$temp°C, $descripcion"

                        when {
                            descripcion.contains("nube", true) -> icono.setImageResource(R.drawable.ic_weather_cloudy)
                            descripcion.contains("lluvia", true) -> icono.setImageResource(R.drawable.ic_weather_rain)
                            descripcion.contains("tormenta", true) -> icono.setImageResource(R.drawable.ic_weather_storm)
                            else -> icono.setImageResource(R.drawable.ic_weather_sun)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("CLIMA", "Error al obtener clima: ${e.localizedMessage}")
                e.printStackTrace()
            }
        }.start()
    }

    private fun actualizarFABs(position: Int) {
        fabInicio.visibility = if (position == 1) View.GONE else View.VISIBLE
        fabRutas.visibility = if (position == 0) View.GONE else View.VISIBLE
        fabCoches.visibility = if (position == 2) View.GONE else View.VISIBLE
    }

    fun animacionBoton(fab: FloatingActionButton) {
        fab.animate()
            .scaleX(0.85f)
            .scaleY(0.85f)
            .setDuration(90)
            .withEndAction {
                fab.animate()
                    .scaleX(1.08f)
                    .scaleY(1.08f)
                    .setDuration(90)
                    .withEndAction {
                        fab.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(80)
                            .start()
                    }
                    .start()
            }
            .start()
    }








    private inner class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 3
        override fun createFragment(position: Int): Fragment = when (position) {
            0 -> RutasFragment()
            1 -> InicioFragment()
            2 -> CochesTabFragment()
            else -> InicioFragment()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun configurarTextSwitcher() {
        switcher.setFactory {
            TextView(this).apply {
                textSize = 25f
                typeface = resources.getFont(R.font.racingsansoneregular)
                setTextColor(resources.getColor(android.R.color.holo_red_light, theme))
                textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                setPadding(15, 20, 15, 15)
            }
        }
        switcher.inAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        switcher.outAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_out)
    }

    private fun cargarPerfilUsuario() {
        val prefs = getSharedPreferences("app", MODE_PRIVATE)
        val token = prefs.getString("jwt", null)

        if (token != null) {
            RetrofitClient.authApi.getPerfil("Bearer $token").enqueue(object : Callback<UserResponse> {
                override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                    val usuario = response.body()
                    mostrarFraseUnica(usuario?.nombre ?: "Conductor")

                    // ✅ Mostrar nombre y correo en el header
                    val navView = findViewById<NavigationView>(R.id.navigationView)
                    val header = navView.getHeaderView(0)
                    val nombreHeader = header.findViewById<TextView>(R.id.headerNombre)
                    val correoHeader = header.findViewById<TextView>(R.id.headerCorreo)

                    nombreHeader.text = usuario?.nombre ?: "Conductor DRIVY"
                    correoHeader.text = usuario?.email ?: "usuario@email.com"
                }

                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    mostrarFraseUnica("Fallo")
                }
            })
        } else {
            mostrarFraseUnica("Token nulo")
        }
    }

    private fun mostrarFraseUnica(nombre: String) {
        val frases = listOf(
            "¡Bienvenido, $nombre!",
            "Qué alegría verte, $nombre.",
            "$nombre, tu coche te espera.",
            "¡A la aventura $nombre!",
            "Vamos a rodar, $nombre."
        )
        val saludo = frases.random()
        switcher.post { switcher.setText(saludo) }
    }
}
