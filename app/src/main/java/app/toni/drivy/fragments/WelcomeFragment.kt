package app.toni.drivy.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import app.toni.drivy.activities.AuthActivity
import app.toni.drivy.databinding.FragmentWelcomeBinding

class WelcomeFragment : Fragment() {

    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)

        // Navegar a Login
        binding.btnIrLogin.setOnClickListener {
            (activity as AuthActivity).cambiarFragmento(LoginFragment())
        }

        // Navegar a Registro
        binding.btnIrRegistro.setOnClickListener {
            (activity as AuthActivity).cambiarFragmento(RegisterFragment())
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
