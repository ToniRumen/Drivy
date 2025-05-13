package app.toni.drivy.fragments.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import app.toni.drivy.R
import app.toni.drivy.dialogs.ModoConduccionDialogFragment

class InicioFragment : Fragment() {

    private lateinit var barraConsumo: ProgressBar
    private lateinit var textoComentario: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_inicio, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ocultar la barra superior
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()

        // Inicializar elementos UI
        barraConsumo = view.findViewById(R.id.barraConsumo)
        textoComentario = view.findViewById(R.id.textoComentario)

        // Simulación del consumo estimado
        val consumoEstimado = (0..100).random()
        actualizarBarraYComentario(consumoEstimado)

        val tarjetaModo = view.findViewById<LinearLayout>(R.id.tarjetaModoConduccion)
        tarjetaModo.setOnClickListener {
            ModoConduccionDialogFragment().show(parentFragmentManager, "ModoDialog")
        }


    }

    private fun actualizarBarraYComentario(consumo: Int) {
        barraConsumo.progress = consumo

        val comentario = when {
            consumo < 30 -> "¡Vamos hoy con calma, muy bien!"
            consumo < 60 -> "Nivel medio, ¡buena ruta!"
            consumo < 85 -> "Hoy toca pisar un poco más."
            else -> "¡Hoy toca competir contra el crono!"
        }

        textoComentario.text = comentario
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Restaurar barra al salir del fragmento
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
    }
}
