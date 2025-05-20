package app.toni.drivy.fragments.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import app.toni.drivy.R
import app.toni.drivy.activities.AuthActivity
import app.toni.drivy.network.models.user.RegisterRequest
import app.toni.drivy.network.RetrofitClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        val nombreInput = view.findViewById<EditText>(R.id.nombreRegister)
        val emailInput = view.findViewById<EditText>(R.id.emailRegister)
        val passwordInput = view.findViewById<EditText>(R.id.passwordRegister)
        val passwordConfirmInput = view.findViewById<EditText>(R.id.passwordConfirmRegister)
        val btnRegister = view.findViewById<Button>(R.id.btnRegister)
        val progressBar = view.findViewById<ProgressBar>(R.id.registerProgressBar)

        btnRegister.setOnClickListener {
            val nombre = nombreInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()
            val passwordConfirm = passwordConfirmInput.text.toString()

            if (nombre.isEmpty() || email.isEmpty() || password.isEmpty() || passwordConfirm.isEmpty()) {
                Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != passwordConfirm) {
                Toast.makeText(requireContext(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            btnRegister.isEnabled = false
            btnRegister.text = "Registrando..."

            val request = RegisterRequest(nombre, email, password)

            RetrofitClient.instance.register(request).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    progressBar.visibility = View.GONE
                    btnRegister.isEnabled = true
                    btnRegister.text = "Registrarse"

                    if (response.isSuccessful && response.body() != null) {
                        Toast.makeText(requireContext(), "¡Registro completado! Por favor, inicia sesión.", Toast.LENGTH_LONG).show()
                        // Redirigir al login:
                        (activity as AuthActivity).cambiarFragmento(LoginFragment())
                    } else {
                        Toast.makeText(requireContext(), "Error al registrarse", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    btnRegister.isEnabled = true
                    btnRegister.text = "Registrarse"
                    Toast.makeText(requireContext(), "Error de red: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        return view
    }
}
