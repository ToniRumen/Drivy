package app.toni.drivy.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import app.toni.drivy.R
import app.toni.drivy.network.RetrofitClient
import app.toni.drivy.network.models.car.CarResponse
import app.toni.drivy.network.models.car.CocheUpdateRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AnadirCocheDialogFragment(private val onAdded: (() -> Unit)? = null) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_editar_coche, null)

        val inputMarca = view.findViewById<EditText>(R.id.inputMarca)
        val inputModelo = view.findViewById<EditText>(R.id.inputModelo)
        val inputAnio = view.findViewById<EditText>(R.id.inputAnio)
        val inputConsumo = view.findViewById<EditText>(R.id.inputConsumo)
        val spinner = view.findViewById<Spinner>(R.id.spinnerCombustible)

        val opciones = listOf("Gasolina", "Diésel", "Híbrido", "Eléctrico")
        spinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, opciones)

        val dialog = AlertDialog.Builder(requireContext()).setView(view).create()

        view.findViewById<Button>(R.id.btnCancelar).setOnClickListener {
            dismiss()
        }

        view.findViewById<Button>(R.id.btnGuardar).setOnClickListener {
            val marca = inputMarca.text.toString().trim()
            val modelo = inputModelo.text.toString().trim()
            val anio = inputAnio.text.toString().toIntOrNull()
            val consumo = inputConsumo.text.toString().toDoubleOrNull()
            val tipo = spinner.selectedItem.toString()

            if (marca.isEmpty() || modelo.isEmpty() || anio == null || consumo == null || tipo.isEmpty()) {
                Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val prefs = requireActivity().getSharedPreferences("app", 0)
            val token = prefs.getString("jwt", null) ?: return@setOnClickListener

            val nuevo = CocheUpdateRequest(marca, modelo, anio, consumo, tipo)

            RetrofitClient.authApi.crearCoche("Bearer $token", nuevo).enqueue(object :
                Callback<CarResponse> {
                override fun onResponse(call: Call<CarResponse>, response: Response<CarResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Coche añadido", Toast.LENGTH_SHORT).show()
                        onAdded?.invoke()
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
