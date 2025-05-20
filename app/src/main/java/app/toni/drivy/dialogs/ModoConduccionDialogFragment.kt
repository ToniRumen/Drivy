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
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_modo_conduccion, null)

        // Referencias
        val eco = view.findViewById<LinearLayout>(R.id.opcionEco)
        val normal = view.findViewById<LinearLayout>(R.id.opcionNormal)
        val sport = view.findViewById<LinearLayout>(R.id.opcionSport)
        val botonElegir = view.findViewById<Button>(R.id.btnElegir)
        textoDescripcion = view.findViewById(R.id.textoDescripcion)

        ecoAnim = view.findViewById(R.id.ecoParticles)
        sportAnim = view.findViewById(R.id.sportParticles)
        fondoDialogo = view

        // ECO
        eco.setOnClickListener {
            modoSeleccionado = "Eco"
            textoDescripcion.text = "Modo Eco activado: consumo eficiente, conducción suave y responsable."
            animarCambioFondo(R.drawable.bg_modo_eco)
            mostrarParticulas("Eco")
        }

        // NORMAL
        normal.setOnClickListener {
            modoSeleccionado = "Normal"
            textoDescripcion.text = "Modo Normal activado: conducción equilibrada para el día a día."
            animarCambioFondo(R.drawable.bg_modo_normal)
            mostrarParticulas("Normal")
        }

        // SPORT
        sport.setOnClickListener {
            modoSeleccionado = "Race"
            textoDescripcion.text = "Modo Race activado: potencia máxima y espíritu competitivo."
            animarCambioFondo(R.drawable.bg_modo_sport)
            mostrarParticulas("Race")
        }

        // BOTÓN ELEGIR
        botonElegir.setOnClickListener {
            if (modoSeleccionado != null) {
                val prefs = requireContext().getSharedPreferences("app", 0)
                prefs.edit().putString("modo_conduccion", modoSeleccionado).apply()

// Animación de salida
                fondoDialogo.animate()
                    .alpha(0f)
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .setDuration(250)
                    .withEndAction {
                        dismiss()
                    }
                    .start()

            } else {
                Toast.makeText(requireContext(), "Selecciona un modo primero", Toast.LENGTH_SHORT).show()
            }

            if (modoSeleccionado != null) {
                val prefs = requireContext().getSharedPreferences("app", 0)
                prefs.edit().putString("modo_conduccion", modoSeleccionado).apply()

                parentFragmentManager.setFragmentResult("modo_seleccionado", Bundle().apply {
                    putString("modo", modoSeleccionado)
                })

                dismiss()
            }

        }

        builder.setView(view)
        return builder.create()
    }

    private fun animarCambioFondo(nuevoFondo: Int) {
        val fondoActual = fondoDialogo.background
        val fondoNuevo = ContextCompat.getDrawable(requireContext(), nuevoFondo)

        val transition = TransitionDrawable(arrayOf(fondoActual, fondoNuevo))
        fondoDialogo.background = transition
        transition.startTransition(400)
    }

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
