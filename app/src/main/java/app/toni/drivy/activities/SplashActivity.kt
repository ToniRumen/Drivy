package app.toni.drivy.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import app.toni.drivy.R

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Aplica tema splash (evita pantalla blanca entre el launcher y el layout)
        setTheme(R.style.Theme_DRIVY_Splash)
        setContentView(R.layout.activity_splash)

        // Aplica animación de entrada al texto de bienvenida
        val welcomeText = findViewById<TextView>(R.id.welcomeText)
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        welcomeText.startAnimation(fadeIn)

        // Obtiene el token de sesión, si existe
        val prefs = getSharedPreferences("app", MODE_PRIVATE)
        val token = prefs.getString("jwt", null)

        // Espera 3 segundos (efecto splash), luego redirige según el estado de sesión
        Handler(Looper.getMainLooper()).postDelayed({
            val destino = if (token != null) MainActivity::class.java else AuthActivity::class.java
            startActivity(Intent(this, destino))
            finish()
        }, 3000)
    }
}
