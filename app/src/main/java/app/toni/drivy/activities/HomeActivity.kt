package app.toni.drivy.activities

import android.os.Bundle
import android.util.Log
import android.widget.TextSwitcher
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import android.view.animation.AnimationUtils
import app.toni.drivy.R
import app.toni.drivy.fragments.menu.InicioFragment
import app.toni.drivy.fragments.menu.RutasFragment
import app.toni.drivy.network.RetrofitClient
import app.toni.drivy.network.UserResponse
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity() {

    private lateinit var switcher: TextSwitcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        switcher = findViewById(R.id.textSwitcherBienvenida)
        configurarTextSwitcher()

        cargarPerfilUsuario()

        // Fragmento inicial
        supportFragmentManager.beginTransaction()
            .replace(R.id.home_fragment_container, InicioFragment())
            .commit()

        findViewById<FloatingActionButton>(R.id.fabNuevaRuta).setOnClickListener {
            cambiarFragment(RutasFragment()) // O muestra una pantalla para añadir nueva ruta
        }


    }

    private fun cambiarFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_drift_in,        // Entrada del nuevo fragment
                R.anim.slide_drift_out,       // Salida del fragment actual
                R.anim.slide_drift_pop_in,    // Al volver atrás
                R.anim.slide_drift_pop_out    // Al salir hacia atrás
            )
            .replace(R.id.home_fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }




    private fun cargarPerfilUsuario() {
        val prefs = getSharedPreferences("app", MODE_PRIVATE)
        val token = prefs.getString("jwt", null)

        if (token != null) {
            RetrofitClient.instance.getPerfil("Bearer $token").enqueue(object : Callback<UserResponse> {
                override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                    val nombre = response.body()?.nombre ?: "Nombre conductor"
                    mostrarFraseUnica(nombre)
                    Log.d("Perfil", "Código: ${response.code()}, cuerpo: ${response.body()?.toString()}")
                    Log.d("Perfil", "RAW JSON: ${response.raw()}")
                    Log.d("Perfil", "JSON body: ${response.body()}")
                    Log.d("Perfil", "Error body: ${response.errorBody()?.string()}")

                }

                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    mostrarFraseUnica("Fallo")
                }
            })
        } else {
            mostrarFraseUnica("Token nulo")
        }
    }

    private fun configurarTextSwitcher() {
        switcher.setFactory {
            TextView(this).apply {
                textSize = 30f
                typeface = resources.getFont(R.font.racingsansoneregular)
                setTextColor(resources.getColor(android.R.color.holo_red_light, theme))
                textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                setPadding(24, 24, 24, 24)
            }
        }
        switcher.inAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        switcher.outAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_out)
    }

    private fun mostrarFraseUnica(nombre: String) {
        val frases = listOf(
            "¡Bienvenido, $nombre!",
            "Qué alegría verte, $nombre.",
            "$nombre, tu coche te espera.",
            "¿Listo para ahorrar gasolina, $nombre?",
            "Vamos a rodar, $nombre."
        )

        val saludo = frases.random()
        switcher.post {
            switcher.setText(saludo)
        }

    }
}
