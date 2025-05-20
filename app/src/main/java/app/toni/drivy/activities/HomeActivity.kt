package app.toni.drivy.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextSwitcher
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import android.view.animation.AnimationUtils
import app.toni.drivy.R
import app.toni.drivy.fragments.coches.CochesTabFragment
import app.toni.drivy.fragments.menu.InicioFragment
import app.toni.drivy.fragments.menu.RutasFragment
import app.toni.drivy.network.RetrofitClient
import app.toni.drivy.network.models.user.UserResponse
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class HomeActivity : AppCompatActivity() {

    private lateinit var switcher: TextSwitcher
    private lateinit var fabInicio: FloatingActionButton
    private lateinit var fabRutas: FloatingActionButton
    private lateinit var fabCoches: FloatingActionButton
    private lateinit var viewPager: ViewPager2

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

        viewPager.adapter = ScreenSlidePagerAdapter(this)
        viewPager.currentItem = 1 // ⬅️ Inicio como página central
        actualizarFABs(1)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                actualizarFABs(position)
            }
        })

        fabInicio.setOnClickListener {
            viewPager.currentItem = 1
        }

        fabRutas.setOnClickListener {
            viewPager.currentItem = 0
        }

        fabCoches.setOnClickListener {
            viewPager.currentItem = 2
        }
    }

    private fun actualizarFABs(position: Int) {
        fabInicio.visibility = if (position == 1) View.GONE else View.VISIBLE
        fabRutas.visibility = if (position == 0) View.GONE else View.VISIBLE
        fabCoches.visibility = if (position == 2) View.GONE else View.VISIBLE
    }

    private inner class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 3
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> RutasFragment()
                1 -> InicioFragment()
                2 -> CochesTabFragment()
                else -> InicioFragment()
            }
        }
    }

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
            RetrofitClient.instance.getPerfil("Bearer $token").enqueue(object : Callback<UserResponse> {
                override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                    val nombre = response.body()?.nombre ?: "Nombre conductor"
                    mostrarFraseUnica(nombre)
                    Log.d("Perfil", "Código: ${response.code()}, cuerpo: ${response.body()?.toString()}")
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
            "¿Listo para ahorrar gasolina, $nombre?",
            "Vamos a rodar, $nombre."
        )

        val saludo = frases.random()
        switcher.post {
            switcher.setText(saludo)
        }
    }
}
