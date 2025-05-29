package app.toni.drivy.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.fragment.app.DialogFragment
import app.toni.drivy.R
import app.toni.drivy.network.RetrofitClient
import app.toni.drivy.network.models.car.CarResponse
import app.toni.drivy.network.models.car.CocheUpdateRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AnadirCocheDialogFragment(
    private val onAdded: (() -> Unit)? = null  // Callback al añadir un coche correctamente
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_editar_coche, null)

        // Referencias a los campos del formulario
        val inputMarca = view.findViewById<EditText>(R.id.inputMarca)
        val inputModelo = view.findViewById<EditText>(R.id.inputModelo)
        val inputAnio = view.findViewById<EditText>(R.id.inputAnio)
        val inputConsumo = view.findViewById<EditText>(R.id.inputConsumo)
        val spinner = view.findViewById<Spinner>(R.id.spinnerCombustible)

        // Opciones fijas para el tipo de combustible
        val opciones = listOf("Gasolina", "Diésel", "Híbrido", "Eléctrico")
        spinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, opciones)

        val dialog = AlertDialog.Builder(requireContext()).setView(view).create()

        // Botón cancelar: cierra el diálogo sin hacer nada
        view.findViewById<Button>(R.id.btnCancelar).setOnClickListener {
            dismiss()
        }

        // Botón guardar: valida y envía los datos al backend
        view.findViewById<Button>(R.id.btnGuardar).setOnClickListener {
            val marca = inputMarca.text.toString().trim()
            val modelo = inputModelo.text.toString().trim()
            val anio = inputAnio.text.toString().toIntOrNull()
            val consumo = inputConsumo.text.toString().toDoubleOrNull()
            val tipo = spinner.selectedItem.toString()

            // Validación de campos
            if (marca.isEmpty() || modelo.isEmpty() || anio == null || consumo == null) {
                Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Recupera el token JWT
            val prefs = requireActivity().getSharedPreferences("app", 0)
            val token = prefs.getString("jwt", null)

            if (token == null) {
                Toast.makeText(requireContext(), "Token no disponible", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Crea el objeto con los datos del coche
            val nuevoCoche = CocheUpdateRequest(marca, modelo, anio, consumo, tipo)

            // Llama al endpoint para crear el coche
            RetrofitClient.authApi.crearCoche("Bearer $token", nuevoCoche)
                .enqueue(object : Callback<CarResponse> {
                    override fun onResponse(call: Call<CarResponse>, response: Response<CarResponse>) {
                        if (response.isSuccessful) {
                            Toast.makeText(requireContext(), "Coche añadido", Toast.LENGTH_SHORT).show()
                            onAdded?.invoke() // Ejecuta callback si se definió
                            dismiss()
                        } else {
                            Toast.makeText(requireContext(), "Error al añadir", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<CarResponse>, t: Throwable) {
                        Toast.makeText(requireContext(), "Error de red: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        return dialog
    }
}
