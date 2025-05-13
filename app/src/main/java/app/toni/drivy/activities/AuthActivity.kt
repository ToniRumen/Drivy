package app.toni.drivy.activities

import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
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
import java.util.*

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var frasesSwitcher: TextSwitcher
    private lateinit var shimmer: ShimmerFrameLayout

    private val frases = listOf(
        "Cada kilómetro deja huella.",
        "Hoy puede empezar algo legendario.",
        "No es solo un coche, es tu libertad.",
        "Pon el destino, DRIVY te acompaña.",
        "La carretera te está esperando.",
        "Acelera tus sueños, no tus dudas."
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        iniciarVideoFondo()
        configurarFrases()
        shimmer = binding.shimmerLogo
        shimmer.startShimmer()

        // Cargar fragmento inicial
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.auth_fragment_container, WelcomeFragment())
                .commit()
        }
    }

    private fun iniciarVideoFondo() {
        val textureView = binding.videoBackground
        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, w: Int, h: Int) {
                mediaPlayer = MediaPlayer()
                val uri = Uri.parse("android.resource://$packageName/${R.raw.video_fondo}")
                mediaPlayer.setDataSource(this@AuthActivity, uri)
                mediaPlayer.setSurface(Surface(surface))
                mediaPlayer.isLooping = true
                mediaPlayer.setVolume(0f, 0f)
                mediaPlayer.setOnPreparedListener { it.start() }
                mediaPlayer.prepareAsync()
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                mediaPlayer.release()
                return true
            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
        }
    }

    private fun configurarFrases() {
        frasesSwitcher = binding.textSwitcher
        frasesSwitcher.setFactory {
            TextView(this).apply {
                textSize = 20f
                setTextColor(resources.getColor(android.R.color.holo_red_light, theme))
                textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                typeface = resources.getFont(R.font.racingsansoneregular)
            }
        }
        frasesSwitcher.inAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        frasesSwitcher.outAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_out)

        val handler = android.os.Handler(mainLooper)
        val updateFrase = object : Runnable {
            var index = 0
            override fun run() {
                frasesSwitcher.setText(frases[index % frases.size])
                index++
                handler.postDelayed(this, 3500)
            }
        }
        handler.post(updateFrase)
    }

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

    override fun onDestroy() {
        super.onDestroy()
        if (::mediaPlayer.isInitialized) mediaPlayer.release()
    }
}
