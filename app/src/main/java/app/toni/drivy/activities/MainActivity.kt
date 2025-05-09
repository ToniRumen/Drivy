package app.toni.drivy.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Accede al archivo de preferencias llamado "drivy_prefs"
        // Aquí guardamos cosas como el token del usuario
        val sharedPref = getSharedPreferences("drivy_prefs", Context.MODE_PRIVATE)

        // Intenta obtener el token que se guardó al hacer login
        // Si no hay token guardado, devuelve null
        val token = sharedPref.getString("token", null)

        // Si ya existe un token, significa que el usuario ya inició sesión anteriormente
        // Por tanto, lo enviamos directamente a la pantalla principal
        if (token != null) {
            startActivity(Intent(this, HomeActivity::class.java))
        } else {
            // Si no hay token, mostramos la pantalla de login
            startActivity(Intent(this, AuthActivity::class.java))
        }

        // Cerramos esta pantalla para que no se quede en el historial
        finish()
    }

}