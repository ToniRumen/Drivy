package app.toni.drivy.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import app.toni.drivy.MainActivity
import app.toni.drivy.R

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_DRIVY_Splash)
        setContentView(R.layout.activity_splash)

        val welcomeText = findViewById<TextView>(R.id.welcomeText)

        // Cargar animación
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        welcomeText.startAnimation(fadeIn)
        welcomeText.visibility = TextView.VISIBLE

        // Después de unos segundos, ir a MainActivity
        Handler().postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 3000) // 3 segundos
    }
}
