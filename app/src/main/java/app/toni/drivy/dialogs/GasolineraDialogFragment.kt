package app.toni.drivy.dialogs

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import app.toni.drivy.R
import app.toni.drivy.network.models.car.Gasolinera

class GasolineraDialogFragment(
    private val gasolineras: List<Gasolinera>,
    private val tipoCombustible: String,
    private val precioEstimado: Double = 1.65, // Recibe el precio estimado (moda)
    private val onActualizar: (() -> Unit)? = null,
    private val onSeleccion: (Gasolinera, Double) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_gasolineras, null)

        // Botón actualizar arriba (puedes ponerlo en tu layout)
        val btnActualizar = view.findViewById<Button>(R.id.btnActualizarGasolineras)
        btnActualizar?.setOnClickListener {
            onActualizar?.invoke()
            dismiss()
        }

        val contenedor = view.findViewById<LinearLayout>(R.id.containerGasolineras)
        gasolineras.forEach { gasolinera ->
            val itemView = inflater.inflate(R.layout.item_gasolinera_card, contenedor, false)
            val precioReal = when (tipoCombustible.lowercase()) {
                "gasolina", "gasolina 95" -> gasolinera.precioGasolina95
                "gasolina 98" -> gasolinera.precioGasolina98
                "diésel", "diesel" -> gasolinera.precioDiesel
                "híbrido", "hibrido" -> gasolinera.precioHibrido
                "eléctrico", "electrico" -> gasolinera.precioElectrico
                else -> gasolinera.precioGasolina95
            }

            // Mostramos el precio real o "desconocido"
            val textPrecio = itemView.findViewById<TextView>(R.id.textPrecio)
            if (precioReal > 0) {
                textPrecio.text = String.format("%.2f €/L", precioReal)
            } else {
                textPrecio.text = "Precio desconocido (${String.format("%.2f €/L", precioEstimado)})"
            }

            itemView.findViewById<TextView>(R.id.textNombre).text = gasolinera.nombre
            itemView.findViewById<TextView>(R.id.textDireccion).text = gasolinera.direccion

            // Botón visitar en Google Maps
            itemView.findViewById<Button>(R.id.btnVisitar).setOnClickListener {
                val uri = Uri.parse("geo:0,0?q=${gasolinera.lat},${gasolinera.lon}(${Uri.encode(gasolinera.nombre)})")
                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                    setPackage("com.google.android.apps.maps")
                }
                if (intent.resolveActivity(requireContext().packageManager) != null) {
                    startActivity(intent)
                }
            }

            // Selección de la gasolinera
            itemView.setOnClickListener {
                // Si el precio real es válido, lo usamos; si no, usamos el estimado
                val precioParaUsar = if (precioReal > 0) precioReal else precioEstimado
                onSeleccion(gasolinera, precioParaUsar)
                dismiss()
            }
            contenedor.addView(itemView)
        }

        builder.setView(view)
        builder.setNegativeButton("Cancelar", null)
        return builder.create()
    }
}
