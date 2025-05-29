package app.toni.drivy.activities

import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.Surface
import android.view.TextureView
import android.view.animation.AnimationUtils
import android.widget.TextSwitcher
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import app.toni.drivy.R
import app.toni.drivy.databinding.ActivityAuthBinding
import app.toni.drivy.fragments.login.WelcomeFragment
import com.facebook.shimmer.ShimmerFrameLayout

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var mediaPlayer: MediaPlayer

    // Frases motivadoras que se rotan en la pantalla de login/registro
    private val frases = listOf(
        "Cada kilómetro deja huella.",
        "Hoy puede empezar algo legendario.",
        "No es solo un coche, es tu libertad.",
        "Pon el destino, DRIVY te acompaña.",
        "La carretera te está esperando.",
        "Acelera tus sueños, no tus dudas."
    )

    // Referencias perezosas a elementos de UI
    private val frasesSwitcher: TextSwitcher by lazy { binding.textSwitcher }
    private val shimmer: ShimmerFrameLayout by lazy { binding.shimmerLogo }

    companion object {
        // Intervalo en milisegundos entre cada frase
        private const val INTERVALO_FRASES_MS = 3500L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Reproduce un video de fondo en bucle (silencioso)
        iniciarVideoFondo()

        // Configura el cambio automático de frases con animaciones
        configurarFrases()

        // Inicia el efecto shimmer sobre el logo
        shimmer.startShimmer()

        // Carga el fragmento inicial (pantalla de bienvenida)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.auth_fragment_container, WelcomeFragment())
                .commit()
        }
    }

    // Inicia la reproducción del video de fondo en el TextureView
    private fun iniciarVideoFondo() {
        binding.videoBackground.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, w: Int, h: Int) {
                mediaPlayer = MediaPlayer().apply {
                    val uri = Uri.parse("android.resource://$packageName/${R.raw.video_fondo}")
                    setDataSource(this@AuthActivity, uri)
                    setSurface(Surface(surface))
                    isLooping = true
                    setVolume(0f, 0f) // Mute
                    setOnPreparedListener { it.start() }
                    prepareAsync()
                }
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                mediaPlayer.release()
                return true
            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
        }
    }

    // Configura el TextSwitcher para mostrar frases con animaciones
    private fun configurarFrases() {
        frasesSwitcher.setFactory { crearTextSwitcherView() }

        frasesSwitcher.inAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        frasesSwitcher.outAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_out)

        val handler = Handler(mainLooper)
        var index = 0
        val fraseRunnable = object : Runnable {
            override fun run() {
                frasesSwitcher.setText(frases[index % frases.size])
                index++
                handler.postDelayed(this, INTERVALO_FRASES_MS)
            }
        }

        // Inicia el cambio de frases
        handler.post(fraseRunnable)
    }

    // Crea el estilo personalizado de cada frase mostrada
    private fun crearTextSwitcherView(): TextView = TextView(this).apply {
        textSize = 20f
        setTextColor(resources.getColor(android.R.color.holo_red_light, theme))
        textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        typeface = resources.getFont(R.font.racingsansoneregular)
    }

    // Método público para cambiar de fragmento con animaciones
    fun cambiarFragmento(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right,
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right
            )
            .replace(R.id.auth_fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
