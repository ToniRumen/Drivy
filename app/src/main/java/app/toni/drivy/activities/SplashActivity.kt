package app.toni.drivy.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import app.toni.drivy.R

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_DRIVY_Splash)
        setContentView(R.layout.activity_splash)

        val welcomeText = findViewById<TextView>(R.id.welcomeText)
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        welcomeText.startAnimation(fadeIn)
        welcomeText.visibility = TextView.VISIBLE

        // Verificar si el usuario ya tiene sesión guardada (token)
        val prefs = getSharedPreferences("app", MODE_PRIVATE)
        val token = prefs.getString("jwt", null)

        Handler().postDelayed({
            if (token != null) {
                // Ya tiene token → va directo al menú principal
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                // No tiene token → va a login/registro
                startActivity(Intent(this, AuthActivity::class.java))
            }
            finish()
        }, 3000)
    }
}
