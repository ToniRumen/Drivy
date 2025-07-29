package app.toni.drivy.fragments.login

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
import app.toni.drivy.network.models.user.LoginRequest
import app.toni.drivy.network.RetrofitClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import android.app.AlertDialog
import android.os.CountDownTimer
import app.toni.drivy.fragments.cargaServer.ServerChecker

class LoginFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        val emailInput = view.findViewById<EditText>(R.id.emailLogin)
        val passwordInput = view.findViewById<EditText>(R.id.passwordLogin)
        val loginButton = view.findViewById<Button>(R.id.btnLogin)
        val botonIrARegistro = view.findViewById<TextView>(R.id.textIrARegistro)
        val progressBar = view.findViewById<ProgressBar>(R.id.loginProgressBar)

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

            progressBar.visibility = View.VISIBLE
            loginButton.isEnabled = false
            loginButton.text = "Cargando..."

            // Primero comprobamos si el servidor está arriba
            ServerChecker.checkServerIsUp { isUp ->
                requireActivity().runOnUiThread {
                    if (isUp) {
                        // Si está arriba, hacemos login normal

                        val request = LoginRequest(email, password)

                        RetrofitClient.authApi.login(request).enqueue(object : Callback<ResponseBody> {
                            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                                progressBar.visibility = View.GONE
                                loginButton.isEnabled = true
                                loginButton.text = "Entrar"

                                if (response.isSuccessful && response.body() != null) {
                                    val token = response.body()!!.string()

                                    val prefs = requireActivity().getSharedPreferences("app", 0)
                                    prefs.edit().putString("jwt", token).apply()

                                    val intent = Intent(requireContext(), MainActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                } else {
                                    Toast.makeText(requireContext(), "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                progressBar.visibility = View.GONE
                                loginButton.isEnabled = true
                                loginButton.text = "Entrar"
                                Toast.makeText(requireContext(), "Error de conexión: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                                Log.e("LoginError", "Fallo de red", t)
                            }
                        })

                    } else {
                        // Si NO está arriba, mostramos el diálogo de espera y volvemos a poner el boton a "Acceder"

                        showWaitingDialog()

                        //PARTE MODIFICADA:
                        progressBar.visibility = View.GONE
                        loginButton.isEnabled = true
                        loginButton.text = "Acceder"
                    }
                }
            }
        }

        return view
    }

    private fun showWaitingDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Cargando servidor...")
        builder.setMessage("Esperando a que el servidor se active...")

        val progressBar = ProgressBar(requireContext(), null, android.R.attr.progressBarStyleHorizontal)
        progressBar.max = 150 // 150 segundos = 2 minutos 30 segundos
        progressBar.progress = 0

        builder.setView(progressBar)
        builder.setCancelable(false)

        val dialog = builder.create()
        dialog.show()

        val timer = object : CountDownTimer(150000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsPassed = (150000 - millisUntilFinished) / 1000
                progressBar.progress = secondsPassed.toInt()
            }

            override fun onFinish() {
                dialog.dismiss()
                Toast.makeText(requireContext(), "Intenta acceder de nuevo", Toast.LENGTH_SHORT).show()
            }
        }
        timer.start()
    }
}
