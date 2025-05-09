package app.toni.drivy.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import app.toni.drivy.R
import app.toni.drivy.activities.AuthActivity


class LoginFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        val botonIrARegistro = view.findViewById<TextView>(R.id.textIrARegistro)
        botonIrARegistro.setOnClickListener {
            (activity as AuthActivity).cambiarFragmento(RegisterFragment())
        }

        return view
    }
}
