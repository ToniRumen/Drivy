package app.toni.drivy.activities

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import app.toni.drivy.R

class PerfilActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        // Obtener extras del intent
        val nombre = intent.getStringExtra("apodo") ?: "Nombre no disponible"
        val correo = intent.getStringExtra("email") ?: "Correo no disponible"


        // Asignar valores a los TextViews
        findViewById<TextView>(R.id.textNombre).text = nombre
        findViewById<TextView>(R.id.textCorreo).text = correo


        // Botón cambiar contraseña (puedes lanzar un intent o lo que necesites aquí)
        findViewById<TextView>(R.id.btnCambiarPassword).setOnClickListener {
            // TODO: Lógica para cambiar contraseña (otro diálogo, intent o actividad)
        }

        // Botón eliminar cuenta con diálogo de confirmación
        findViewById<TextView>(R.id.btnEliminarCuenta).setOnClickListener {
            mostrarDialogoEliminarCuenta()
        }
    }

    private fun mostrarDialogoEliminarCuenta() {
        AlertDialog.Builder(this)
            .setTitle("¿Eliminar cuenta?")
            .setMessage("Esta acción no se puede deshacer. ¿Estás seguro?")
            .setPositiveButton("Sí, eliminar") { _, _ ->
                eliminarCuenta()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarCuenta() {
        // TODO: Aquí iría la lógica real para eliminar la cuenta (por ejemplo, llamada a la API + cerrar sesión)
        // Por ahora simplemente cierra la actividad como ejemplo
        finishAffinity()
    }
}
