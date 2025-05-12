package app.toni.drivy.fragments

import android.util.Log
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import app.toni.drivy.R
import app.toni.drivy.activities.AuthActivity
import app.toni.drivy.activities.MainActivity
import app.toni.drivy.network.LoginRequest
import app.toni.drivy.network.RetrofitClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        val emailInput = view.findViewById<EditText>(R.id.emailLogin)
        val passwordInput = view.findViewById<EditText>(R.id.passwordLogin)
        val loginButton = view.findViewById<Button>(R.id.btnLogin)
        val botonIrARegistro = view.findViewById<TextView>(R.id.textIrARegistro)

        // Navegación a registro
        botonIrARegistro.setOnClickListener {
            (activity as AuthActivity).cambiarFragmento(RegisterFragment())
        }

        // Lógica de login
        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = LoginRequest(email, password)

            RetrofitClient.instance.login(request).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful && response.body() != null) {
                        val token = response.body()!!.string()

                        // Guardar JWT
                        val prefs = requireActivity().getSharedPreferences("app", 0)
                        prefs.edit().putString("jwt", token).apply()

                        Toast.makeText(requireContext(), "Login correcto", Toast.LENGTH_SHORT).show()

                        // Ir a MainActivity
                        val intent = Intent(requireContext(), MainActivity::class.java)
                        startActivity(intent)
                        requireActivity().finish()

                    } else {
                        Toast.makeText(requireContext(), "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error de conexión: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                    Log.e("LoginError", "Fallo de red", t)
                }
            })
        }

        return view
    }
}
