package app.toni.drivy.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import app.toni.drivy.R
import app.toni.drivy.activities.AuthActivity
import app.toni.drivy.network.RegisterRequest
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

            val request = RegisterRequest(nombre, email, password)

            RetrofitClient.instance.register(request).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Registro exitoso. Ahora puedes iniciar sesión", Toast.LENGTH_LONG).show()
                        (activity as AuthActivity).cambiarFragmento(LoginFragment())
                    } else {
                        Toast.makeText(requireContext(), "Error al registrarse", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error de red: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        return view
    }
}
