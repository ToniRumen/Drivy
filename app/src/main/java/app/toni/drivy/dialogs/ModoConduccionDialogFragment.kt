package app.toni.drivy.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import app.toni.drivy.R
import com.airbnb.lottie.LottieAnimationView

class ModoConduccionDialogFragment : DialogFragment() {

    private var modoSeleccionado: String? = null

    private lateinit var textoDescripcion: TextView
    private lateinit var ecoAnim: LottieAnimationView
    private lateinit var sportAnim: LottieAnimationView
    private lateinit var fondoDialogo: View

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext(), R.style.DialogTheme)
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_modo_conduccion, null)

        // Referencias UI
        val opcionEco = view.findViewById<LinearLayout>(R.id.opcionEco)
        val opcionNormal = view.findViewById<LinearLayout>(R.id.opcionNormal)
        val opcionSport = view.findViewById<LinearLayout>(R.id.opcionSport)
        val botonElegir = view.findViewById<Button>(R.id.btnElegir)

        textoDescripcion = view.findViewById(R.id.textoDescripcion)
        ecoAnim = view.findViewById(R.id.ecoParticles)
        sportAnim = view.findViewById(R.id.sportParticles)
        fondoDialogo = view

        // Selección de modo ECO
        opcionEco.setOnClickListener {
            modoSeleccionado = "Eco"
            textoDescripcion.text = "Modo Eco activado: consumo eficiente, conducción suave y responsable."
            animarCambioFondo(R.drawable.bg_modo_eco)
            mostrarParticulas("Eco")
        }

        // Selección de modo NORMAL
        opcionNormal.setOnClickListener {
            modoSeleccionado = "Normal"
            textoDescripcion.text = "Modo Normal activado: conducción equilibrada para el día a día."
            animarCambioFondo(R.drawable.bg_modo_normal)
            mostrarParticulas("Normal")
        }

        // Selección de modo SPORT
        opcionSport.setOnClickListener {
            modoSeleccionado = "Race"
            textoDescripcion.text = "Modo Race activado: potencia máxima y espíritu competitivo."
            animarCambioFondo(R.drawable.bg_modo_sport)
            mostrarParticulas("Race")
        }

        // Confirmación del modo elegido
        botonElegir.setOnClickListener {
            if (modoSeleccionado == null) {
                Toast.makeText(requireContext(), "Selecciona un modo primero", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            guardarModoConduccion(modoSeleccionado!!)
            animarSalidaYCerrar()
        }

        builder.setView(view)
        return builder.create()
    }

    /**
     * Guarda el modo seleccionado en SharedPreferences
     * y lo comunica al fragmento que abrió el diálogo.
     */
    private fun guardarModoConduccion(modo: String) {
        val prefs = requireContext().getSharedPreferences("app", 0)
        prefs.edit().putString("modo_conduccion", modo).apply()

        parentFragmentManager.setFragmentResult("modo_seleccionado", Bundle().apply {
            putString("modo", modo)
        })
    }

    /**
     * Aplica animación de salida y luego cierra el diálogo.
     */
    private fun animarSalidaYCerrar() {
        fondoDialogo.animate()
            .alpha(0f)
            .scaleX(0.9f)
            .scaleY(0.9f)
            .setDuration(250)
            .withEndAction { dismiss() }
            .start()
    }

    /**
     * Realiza una transición suave del fondo al cambiar de modo.
     */
    private fun animarCambioFondo(nuevoFondo: Int) {
        val fondoActual = fondoDialogo.background
        val fondoNuevo = ContextCompat.getDrawable(requireContext(), nuevoFondo)

        val transition = TransitionDrawable(arrayOf(fondoActual, fondoNuevo))
        fondoDialogo.background = transition
        transition.startTransition(400)
    }

    /**
     * Muestra animaciones de partículas según el modo seleccionado.
     */
    private fun mostrarParticulas(modo: String) {
        when (modo) {
            "Eco" -> {
                ecoAnim.setAnimation("eco_particles.json")
                ecoAnim.visibility = View.VISIBLE
                ecoAnim.playAnimation()

                sportAnim.visibility = View.GONE
                sportAnim.cancelAnimation()
            }

            "Race" -> {
                sportAnim.setAnimation("flames_or_flags.json")
                sportAnim.visibility = View.VISIBLE
                sportAnim.playAnimation()

                ecoAnim.visibility = View.GONE
                ecoAnim.cancelAnimation()
            }

            "Normal" -> {
                ecoAnim.visibility = View.GONE
                ecoAnim.cancelAnimation()
                sportAnim.visibility = View.GONE
                sportAnim.cancelAnimation()
            }
        }
    }
}
