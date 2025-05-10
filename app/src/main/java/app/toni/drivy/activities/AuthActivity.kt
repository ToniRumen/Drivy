package app.toni.drivy.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import app.toni.drivy.R
import app.toni.drivy.fragments.LoginFragment
import app.toni.drivy.fragments.WelcomeFragment

class AuthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        // Cargar el fragmento de login por defecto
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.auth_fragment_container, WelcomeFragment())
                .commit()
        }
    }

    // Función pública para cambiar de fragmento con animación
    fun cambiarFragmento(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                android.R.anim.slide_in_left,  // entrada
                android.R.anim.slide_out_right, // salida
                android.R.anim.slide_in_left,   // reentrada
                android.R.anim.slide_out_right  // resalida
            )
            .replace(R.id.auth_fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
