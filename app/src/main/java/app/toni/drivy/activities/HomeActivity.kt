package app.toni.drivy.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import androidx.preference.PreferenceManager
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
import java.util.Locale
import kotlin.math.abs

class HomeActivity : AppCompatActivity() {

    // Componentes principales de la UI
    private lateinit var switcher: TextSwitcher
    private lateinit var fabInicio: FloatingActionButton
    private lateinit var fabRutas: FloatingActionButton
    private lateinit var fabCoches: FloatingActionButton
    private lateinit var viewPager: ViewPager2

    // Cliente HTTP para peticiones externas
    private val client = OkHttpClient()

    @RequiresApi(Build.VERSION_CODES.O)


    override fun attachBaseContext(newBase: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(newBase)
        val lang = prefs.getString("app_language", "es") ?: "es"
        val locale = Locale(lang)
        Locale.setDefault(locale)

        val config = Configuration()
        config.setLocale(locale)
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }





    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Referencias a componentes visuales
        switcher = findViewById(R.id.textSwitcherBienvenida)
        fabInicio = findViewById(R.id.fabInicio)
        fabRutas = findViewById(R.id.fabRutas)
        fabCoches = findViewById(R.id.fabCoches)
        viewPager = findViewById(R.id.viewPager)

        configurarTextSwitcher()
        cargarPerfilUsuario()
        configurarViewPager()
        configurarDrawer()
        mostrarTipDelDia()
        cargarClimaActual()
    }

    // Configura la navegación entre pantallas con ViewPager2
    private fun configurarViewPager() {
        viewPager.adapter = ScreenSlidePagerAdapter(this)
        viewPager.currentItem = 1 // Pantalla de inicio por defecto
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
    }

    // Configura el menú lateral (Drawer)
    private fun configurarDrawer() {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        val navView = findViewById<NavigationView>(R.id.navigationView)
        findViewById<ImageButton>(R.id.btnHamburguesa).setOnClickListener {
            drawerLayout.openDrawer(Gravity.START)
        }

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_perfil -> toast(getString(R.string.perfil_no_disponible))
                R.id.nav_historial -> viewPager.currentItem = 0
                R.id.nav_ajustes -> {

                    startActivity(Intent(this, SettingsActivity::class.java))
                    true

                }
                R.id.nav_logout -> {
                    // Elimina el JWT guardado y vuelve a la pantalla de login
                    getSharedPreferences("app", MODE_PRIVATE).edit().remove("jwt").apply()
                    startActivity(Intent(this, AuthActivity::class.java))
                    finish()
                }
            }
            drawerLayout.closeDrawers()
            true
        }
    }

    // Muestra un tip aleatorio diario en la parte inferior
    private fun mostrarTipDelDia() {
        val tips = listOf(
            getString(R.string.tip_1),
            getString(R.string.tip_2),
            getString(R.string.tip_3),
            getString(R.string.tip_4),
            getString(R.string.tip_5)
        )

        findViewById<TextView>(R.id.textoTipDelDia).text = tips.random()
    }

    // Inicia la obtención de ubicación y posterior carga del clima
    private fun cargarClimaActual() {
        if (!checkLocationPermission()) return

        val fused = LocationServices.getFusedLocationProviderClient(this)
        fused.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                obtenerClima(location.latitude, location.longitude)
            } else {
                Log.e("CLIMA", "Ubicación es null")
            }
        }
    }

    // Comprueba y solicita permiso para acceder a la ubicación
    private fun checkLocationPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
            return false
        }
        return true
    }

    // Llama a la API de OpenWeather y muestra el clima actual
    private fun obtenerClima(lat: Double, lon: Double) {
        val apiKey = "a84eff9d37fbd02f031d5a8825ef959c"
        val url = "https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&units=metric&lang=es&appid=$apiKey"

        Thread {
            try {
                val response = client.newCall(Request.Builder().url(url).build()).execute()
                val body = response.body?.string() ?: return@Thread
                val json = JSONObject(body)
                val temp = json.getJSONObject("main").getDouble("temp").toInt()
                val desc = json.getJSONArray("weather").getJSONObject(0).getString("description")

                runOnUiThread {
                    findViewById<TextView>(R.id.textoClima).text = "$temp°C, $desc"
                    val icono = findViewById<ImageView>(R.id.iconoClima)
                    when {
                        desc.contains("nube", true) -> icono.setImageResource(R.drawable.ic_weather_cloudy)
                        desc.contains("lluvia", true) -> icono.setImageResource(R.drawable.ic_weather_rain)
                        desc.contains("tormenta", true) -> icono.setImageResource(R.drawable.ic_weather_storm)
                        else -> icono.setImageResource(R.drawable.ic_weather_sun)
                    }
                }
            } catch (e: Exception) {
                Log.e("CLIMA", "Error clima: ${e.localizedMessage}")
            }
        }.start()
    }

    // Muestra u oculta los FABs según la página actual
    private fun actualizarFABs(position: Int) {
        fabInicio.visibility = if (position == 1) View.GONE else View.VISIBLE
        fabRutas.visibility = if (position == 0) View.GONE else View.VISIBLE
        fabCoches.visibility = if (position == 2) View.GONE else View.VISIBLE
    }

    // Muestra un toast (mensaje flotante) corto
    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    // Efecto de rebote animado al pulsar un FAB
    fun animacionBoton(fab: FloatingActionButton, dur: Long = 90L) {
        fab.animate()
            .scaleX(0.85f).scaleY(0.85f).setDuration(dur)
            .withEndAction {
                fab.animate()
                    .scaleX(1.08f).scaleY(1.08f).setDuration(dur)
                    .withEndAction {
                        fab.animate().scaleX(1f).scaleY(1f).setDuration(dur).start()
                    }
                    .start()
            }
            .start()
    }

    // Adaptador de fragmentos del ViewPager
    private inner class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 3
        override fun createFragment(position: Int): Fragment = when (position) {
            0 -> RutasFragment()
            1 -> InicioFragment()
            2 -> CochesTabFragment()
            else -> InicioFragment()
        }
    }

    // Configura el TextSwitcher para mostrar frases de bienvenida animadas
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

    // Llama a la API y muestra el nombre del usuario en el menú lateral
    private fun cargarPerfilUsuario() {
        val prefs = getSharedPreferences("app", MODE_PRIVATE)
        val token = prefs.getString("jwt", null) ?: return mostrarFraseUnica("Token nulo")

        RetrofitClient.authApi.getPerfil("Bearer $token").enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {

                if (response.isSuccessful){

                    val usuario = response.body()
                    mostrarFraseUnica(usuario?.nombre ?: "Conductor")

                    val navView = findViewById<NavigationView>(R.id.navigationView)
                    val header = navView.getHeaderView(0)
                    header.findViewById<TextView>(R.id.headerNombre).text = usuario?.nombre ?: "Conductor DRIVY"
                    header.findViewById<TextView>(R.id.headerCorreo).text = usuario?.email ?: "usuario@email.com"
                } else {
                    // Respuesta errónea del servidor (p.ej. 500)
                    manejarErrorBackend()
                }

            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                manejarErrorBackend()
            }
        })
    }

    // Maneja el error cerrando sesión y mostrando toast
    private fun manejarErrorBackend() {
        runOnUiThread {
            // Eliminar JWT para forzar logout
            getSharedPreferences("app", MODE_PRIVATE).edit().remove("jwt").apply()

            toast("El servidor está arrancando, cierra sesión y vuelve a intentarlo en unos segundos")

            // Lanzar Activity de login y cerrar Home
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
        }
    }

    // Elige una frase aleatoria de bienvenida con el nombre del usuario
    private fun mostrarFraseUnica(nombre: String) {
        val frases = listOf(
            getString(R.string.bienvenida_1, nombre),
            getString(R.string.bienvenida_2, nombre),
            getString(R.string.bienvenida_3, nombre),
            getString(R.string.bienvenida_4, nombre),
            getString(R.string.bienvenida_5, nombre)
        )

        switcher.post { switcher.setText(frases.random()) }
    }

    // Efecto visual de transición entre pantallas del ViewPager
    class FadeSlidePageTransformer : ViewPager2.PageTransformer {
        override fun transformPage(page: View, position: Float) {
            page.apply {
                when {
                    position < -1 || position > 1 -> alpha = 0f
                    else -> {
                        alpha = 1 - abs(position)
                        translationX = -position * width * 0.3f
                        scaleY = 0.95f + (1 - abs(position)) * 0.05f
                    }
                }
            }
        }
    }


}
