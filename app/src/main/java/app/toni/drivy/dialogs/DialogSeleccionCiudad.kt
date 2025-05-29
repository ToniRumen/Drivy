package app.toni.drivy.dialogs

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode

class DialogSeleccionCiudad : DialogFragment() {

    private var origen: Place? = null
    private var destino: Place? = null

    // Lanzador para recibir el resultado del intent de Google Places
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK && result.data != null) {
            val place = Autocomplete.getPlaceFromIntent(result.data!!)
            Log.d("PLACES_RESULT", "Lugar seleccionado: ${place.name} (${place.latLng})")

            if (origen == null) {
                origen = place
                iniciarSelectorLugar() // Lanza de nuevo para seleccionar destino
            } else {
                destino = place
                enviarResultadoYSalir() // Ya tenemos origen y destino
            }
        } else {
            Toast.makeText(requireContext(), "Selección cancelada o error", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // Sin barra de título

        // Inicializa Google Places si aún no está listo
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), "AIzaSyDEyTgGFym-4Nci_cDiWOy-wzRPB2jJBU0")
        }

        iniciarSelectorLugar() // Inicia el proceso de selección (primero el origen)

        return dialog
    }

    /**
     * Lanza el intent de Autocomplete para que el usuario elija una ciudad.
     */
    private fun iniciarSelectorLugar() {
        try {
            val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .build(requireContext())
            launcher.launch(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error al iniciar el selector de lugares", Toast.LENGTH_LONG).show()
            dismiss()
        }
    }

    /**
     * Envía el origen y destino seleccionados como resultado al fragmento que lo invocó.
     */
    private fun enviarResultadoYSalir() {
        if (origen == null || destino == null) {
            Toast.makeText(requireContext(), "No se han seleccionado ambas ciudades", Toast.LENGTH_SHORT).show()
            dismiss()
            return
        }

        val result = Bundle().apply {
            putString("origen_nombre", origen!!.name)
            putString("destino_nombre", destino!!.name)
            putParcelable("origen_latlng", origen!!.latLng)
            putParcelable("destino_latlng", destino!!.latLng)
        }

        parentFragmentManager.setFragmentResult("ruta_personalizada", result)
        dismiss()
    }
}
