package app.toni.drivy.fragments.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextSwitcher
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import app.toni.drivy.R

class InicioFragment : Fragment() {

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



    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Restaurar barra al salir del fragmento
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
    }
}
