package app.toni.drivy.fragments

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextSwitcher
import android.widget.TextView
import androidx.fragment.app.Fragment
import app.toni.drivy.activities.AuthActivity

import app.toni.drivy.databinding.FragmentWelcomeBinding

class WelcomeFragment : Fragment() {

    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!

    private val frases = listOf(
        "¿Estás listo para arrancar?",
        "Haz que cada kilómetro cuente.",
        "Hoy puede comenzar tu mejor ruta.",
        "Tú decides el destino.",
        "Pon el corazón en la carretera.",
        "No es solo conducir, es vivir.",
        "La aventura comienza con un clic.",
        "Explora. Descubre. Repite.",
        "Convierte rutas en recuerdos.",
        "Atrévete a salir de la rutina."
    )

    private var fraseIndex = 0
    private lateinit var textSwitcher: TextSwitcher

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)

        binding.btnIrLogin.setOnClickListener {
            (activity as AuthActivity).cambiarFragmento(LoginFragment())
        }

        binding.btnIrRegistro.setOnClickListener {
            (activity as AuthActivity).cambiarFragmento(RegisterFragment())
        }

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textSwitcher = binding.textSwitcher
        textSwitcher.setFactory {
            TextView(requireContext()).apply {
                textSize = 18f
                setTextColor(Color.WHITE)
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            }
        }

        textSwitcher.setInAnimation(context, android.R.anim.slide_in_left)
        textSwitcher.setOutAnimation(context, android.R.anim.slide_out_right)

        // Cambiar frase cada 3 segundos
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                textSwitcher.setText(frases[fraseIndex])
                fraseIndex = (fraseIndex + 1) % frases.size
                handler.postDelayed(this, 3000)
            }
        }
        handler.post(runnable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }




}
