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
    private val onSeleccion: (Gasolinera) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_gasolineras, null)

        val contenedor = view.findViewById<LinearLayout>(R.id.containerGasolineras)

        gasolineras.forEach { gasolinera ->
            val itemView = inflater.inflate(R.layout.item_gasolinera_card, contenedor, false)

            itemView.findViewById<TextView>(R.id.textNombre).text = gasolinera.nombre
            itemView.findViewById<TextView>(R.id.textDireccion).text = gasolinera.direccion
            itemView.findViewById<TextView>(R.id.textPrecio).text =
                String.format("%.2f €/L", gasolinera.precioGasolina95)

            itemView.findViewById<Button>(R.id.btnVisitar).setOnClickListener {
                val uri = Uri.parse("geo:0,0?q=${gasolinera.lat},${gasolinera.lon}(${Uri.encode(gasolinera.nombre)})")
                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                    setPackage("com.google.android.apps.maps")
                }
                if (intent.resolveActivity(requireContext().packageManager) != null) {
                    startActivity(intent)
                }
            }

            itemView.setOnClickListener {
                onSeleccion(gasolinera)
                dismiss()
            }

            contenedor.addView(itemView)
        }

        builder.setView(view)
        builder.setNegativeButton("Cancelar", null)
        return builder.create()
    }
}

