package app.toni.drivy.fragments.login

import android.app.AlertDialog
import android.os.Bundle
import android.os.CountDownTimer
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
import java.text.Normalizer
import java.util.Locale
import app.toni.drivy.fragments.cargaServer.ServerChecker


class RegisterFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        val nombreInput = view.findViewById<EditText>(R.id.nombreRegister)
        val emailInput = view.findViewById<EditText>(R.id.emailRegister)
        val passwordInput = view.findViewById<EditText>(R.id.passwordRegister)
        val passwordConfirmInput = view.findViewById<EditText>(R.id.passwordConfirmRegister)
        val btnRegister = view.findViewById<Button>(R.id.btnRegister)
        val progressBar = view.findViewById<ProgressBar>(R.id.registerProgressBar)



        fun normalizarTexto(texto: String): String {
            val textoSinAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
            return textoSinAcentos.lowercase(Locale.ROOT)
        }

        fun contieneContenidoProhibido(nombre: String): Boolean {
            val nombreNormalizado = normalizarTexto(nombre)
                .replace(Regex("[^a-z]"), "") // Elimina símbolos y números para evitar trampas

            //Lista de nombres prohibidos para el registro:
            val palabrasProhibidas = listOf(
                // Extremistas
                "hitler", "nazi", "ss", "gestapo", "fascista", "fascism", "franco", "dictador",
                "racista", "racism", "xenofobo", "xenofobia", "homofobo", "homofobia",
                "terrorista", "terrorismo", "isis", "alqaeda", "caudillo",

                // Suplantación / autoridad
                "admin", "administrador", "moderador", "mod", "staff", "soporte", "support",
                "root", "sysadmin", "dev", "developer", "official", "oficial",

                // Insultos / vulgaridades
                "puta", "puto", "mierda", "gilipollas", "coño", "cabron", "polla", "pedo",
                "maricon", "culero", "zorra", "imbecil", "estupido", "idiota", "verga",
                "pendejo", "jodido", "tonto", "subnormal", "retardado", "retrasado"
            )


            return palabrasProhibidas.any { nombreNormalizado.contains(it) }
        }

        fun contieneEmojisONoLetras(nombre: String): Boolean {
            // Detecta caracteres que no sean letras o espacios
            return nombre.any { !it.isLetter() && !it.isWhitespace() }
        }

        fun excesoMayusculas(nombre: String): Boolean {
            val soloLetras = nombre.filter { it.isLetter() }
            val mayusculas = soloLetras.count { it.isUpperCase() }
            return mayusculas > soloLetras.length * 0.7 // Más del 70% en mayúsculas
        }

        btnRegister.setOnClickListener {
            val nombre = nombreInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()
            val passwordConfirm = passwordConfirmInput.text.toString()

            if (nombre.isEmpty() || email.isEmpty() || password.isEmpty() || passwordConfirm.isEmpty()) {
                Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (nombre.length <= 3){
                Toast.makeText(requireContext(), "La longitud debe ser mayor a 3", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (contieneContenidoProhibido(nombre)) {
                Toast.makeText(requireContext(), "Ese apodo no está permitido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (contieneEmojisONoLetras(nombre)) {
                Toast.makeText(requireContext(), "No se permiten símbolos, emojis ni números en el apodo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (excesoMayusculas(nombre)) {
                Toast.makeText(requireContext(), "Usa un formato de texto normal, sin tantas mayúsculas", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(requireContext(), "Introduce un correo válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }




            if (password != passwordConfirm) {
                Toast.makeText(requireContext(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 8) {
                Toast.makeText(requireContext(), "La contraseña debe tener al menos 8 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val progressBar = view.findViewById<ProgressBar>(R.id.registerProgressBar)
            btnRegister.isEnabled = false
            btnRegister.text = "Cargando..."
            progressBar.visibility = View.VISIBLE
            btnRegister.text = "Registrando..."


            ServerChecker.checkServerIsUp { isUp ->
                requireActivity().runOnUiThread {
                    if (isUp) {
                        val request = RegisterRequest(nombre, email, password)

                        RetrofitClient.authApi.register(request).enqueue(object : Callback<ResponseBody> {
                            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                                progressBar.visibility = View.GONE
                                btnRegister.isEnabled = true
                                btnRegister.text = "Registrarse"

                                if (response.isSuccessful) {
                                    Toast.makeText(requireContext(), "Registro exitoso. Ahora puedes iniciar sesión", Toast.LENGTH_LONG).show()
                                    (activity as AuthActivity).cambiarFragmento(LoginFragment())
                                } else {
                                    Toast.makeText(requireContext(), "No se pudo registrar. Intenta con otro correo", Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                progressBar.visibility = View.GONE
                                btnRegister.isEnabled = true
                                btnRegister.text = "Registrarse"
                                Toast.makeText(requireContext(), "Error de conexión: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        })
                    } else {
                        showWaitingDialog()
                        progressBar.visibility = View.GONE
                        btnRegister.isEnabled = true
                        btnRegister.text = "Registrarse"
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
                Toast.makeText(requireContext(), "Intenta registrarte de nuevo", Toast.LENGTH_SHORT).show()

            }
        }
        timer.start()
    }

}
