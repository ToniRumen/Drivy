package app.toni.drivy.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.libraries.places.api.Places

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa Google Places si aún no está activo (evita crashes)
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyDEyTgGFym-4Nci_cDiWOy-wzRPB2jJBU0")
        }

        // Accede a las preferencias compartidas donde se guarda el token del usuario
        val prefs = getSharedPreferences("app", Context.MODE_PRIVATE)
        val token = prefs.getString("jwt", null)

        // Si hay token, se redirige al menú principal; si no, a la pantalla de login
        val destino = if (token != null) HomeActivity::class.java else AuthActivity::class.java
        startActivity(Intent(this, destino))

        // Cierra esta actividad para no dejarla en el historial (back stack)
        finish()
    }
}
