package app.toni.drivy.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import app.toni.drivy.R
import app.toni.drivy.activities.AuthActivity

class WelcomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_welcome, container, false)

        val btnLogin = view.findViewById<Button>(R.id.btnIrLogin)
        val btnRegister = view.findViewById<Button>(R.id.btnIrRegistro)

        btnLogin.setOnClickListener {
            (activity as AuthActivity).cambiarFragmento(LoginFragment())
        }

        btnRegister.setOnClickListener {
            (activity as AuthActivity).cambiarFragmento(RegisterFragment())
        }

        return view
    }
}
