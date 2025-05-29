package app.toni.drivy.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import app.toni.drivy.R
import app.toni.drivy.network.RetrofitClient
import app.toni.drivy.network.models.car.CarResponse
import app.toni.drivy.network.models.car.CocheUpdateRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditarCocheDialogFragment : DialogFragment() {

    private var onUpdatedCallback: (() -> Unit)? = null

    companion object {
        /**
         * Crea una nueva instancia del diálogo con el coche a editar y un callback opcional.
         */
        fun newInstance(coche: CarResponse, onUpdated: (() -> Unit)? = null): EditarCocheDialogFragment {
            return EditarCocheDialogFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("arg_coche", coche)
                }
                setOnUpdatedCallback(onUpdated)
            }
        }
    }

    /**
     * Permite asignar el callback externo para cuando se actualiza el coche.
     */
    fun setOnUpdatedCallback(callback: (() -> Unit)?) {
        this.onUpdatedCallback = callback
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val coche = arguments?.getSerializable("arg_coche") as CarResponse

        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_editar_coche, null)

        // Referencias a los campos del formulario
        val inputMarca = view.findViewById<EditText>(R.id.inputMarca)
        val inputModelo = view.findViewById<EditText>(R.id.inputModelo)
        val inputAnio = view.findViewById<EditText>(R.id.inputAnio)
        val inputConsumo = view.findViewById<EditText>(R.id.inputConsumo)
        val spinnerCombustible = view.findViewById<Spinner>(R.id.spinnerCombustible)

        // Configura las opciones del spinner
        val opciones = listOf("Gasolina", "Diésel", "Híbrido", "Eléctrico")
        spinnerCombustible.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, opciones)

        // Rellenar datos actuales del coche
        inputMarca.setText(coche.marca)
        inputModelo.setText(coche.modelo)
        inputAnio.setText(coche.anio.toString())
        inputConsumo.setText(coche.consumoMedio.toString())

        // Preselección de tipo de combustible
        val indexCombustible = opciones.indexOfFirst { it.equals(coche.tipoCombustible, ignoreCase = true) }
        if (indexCombustible != -1) spinnerCombustible.setSelection(indexCombustible)

        // Crea el diálogo de alerta con animación personalizada (si tienes styles definidos)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .create()

        dialog.window?.attributes?.windowAnimations = R.style.DialogFadeAnimation

        // Botón cancelar
        view.findViewById<Button>(R.id.btnCancelar).setOnClickListener {
            dismiss()
        }

        // Botón guardar
        view.findViewById<Button>(R.id.btnGuardar).setOnClickListener {
            val marca = inputMarca.text.toString().trim()
            val modelo = inputModelo.text.toString().trim()
            val anio = inputAnio.text.toString().toIntOrNull()
            val consumo = inputConsumo.text.toString().toDoubleOrNull()
            val tipo = spinnerCombustible.selectedItem.toString()

            // Validación de campos
            if (marca.isEmpty() || modelo.isEmpty() || anio == null || consumo == null) {
                Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val prefs = requireActivity().getSharedPreferences("app", 0)
            val token = prefs.getString("jwt", null) ?: return@setOnClickListener

            val update = CocheUpdateRequest(marca, modelo, anio, consumo, tipo)

            // Llama al backend para actualizar
            RetrofitClient.authApi.actualizarCoche(coche.id, "Bearer $token", update)
                .enqueue(object : Callback<CarResponse> {
                    override fun onResponse(call: Call<CarResponse>, response: Response<CarResponse>) {
                        if (response.isSuccessful) {
                            Toast.makeText(requireContext(), "Coche actualizado", Toast.LENGTH_SHORT).show()
                            onUpdatedCallback?.invoke()
                            dismiss()
                        } else {
                            Toast.makeText(requireContext(), "Error al actualizar", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<CarResponse>, t: Throwable) {
                        Toast.makeText(requireContext(), "Fallo de red: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        return dialog
    }
}
