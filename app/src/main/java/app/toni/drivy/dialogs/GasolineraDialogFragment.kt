package app.toni.drivy.dialogs

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import app.toni.drivy.R
import app.toni.drivy.network.models.car.Gasolinera
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class GasolineraDialogFragment(
    private val gasolineras: List<Gasolinera>,
    private val onSeleccion: (Gasolinera) -> Unit
) : DialogFragment() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var prefs: android.content.SharedPreferences
    private lateinit var contenedor: LinearLayout
    private var ubicacionActual: Location? = null
    private lateinit var tipoCombustible: String

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_gasolineras, null)

        contenedor = view.findViewById(R.id.containerGasolineras)
        val btnActualizar = view.findViewById<Button>(R.id.btnActualizarGasolineras)
        val anim = AnimationUtils.loadAnimation(requireContext(), R.anim.boton_reflejo)

        prefs = requireContext().getSharedPreferences("app", 0)
        tipoCombustible = prefs.getString("tipo_combustible", null)?.lowercase() ?: "gasolina"
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        mostrarGasolineras(gasolineras)

        // Obtener ubicación inicial para mostrar los km desde el principio
        obtenerUbicacionSinOrdenar()

        btnActualizar.setOnClickListener {
            btnActualizar.isEnabled = false
            btnActualizar.startAnimation(anim)
            btnActualizar.text = "Actualizando..."

            obtenerUbicacionYReordenar {
                btnActualizar.clearAnimation()
                btnActualizar.isEnabled = true
                btnActualizar.text = "Actualizar"
            }
        }

        builder.setView(view)
        return builder.create()
    }

    private fun obtenerUbicacionSinOrdenar() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return // no mostrar error aquí, es opcional
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                ubicacionActual = location
                mostrarGasolineras(gasolineras) // actualiza la lista con km visibles
            }
        }
    }


    private fun mostrarGasolineras(lista: List<Gasolinera>) {
        contenedor.removeAllViews()
        val inflater = requireActivity().layoutInflater

        lista.forEach { gasolinera ->
            val itemView = inflater.inflate(R.layout.item_gasolinera_card, contenedor, false)

            itemView.findViewById<TextView>(R.id.textNombre).text = gasolinera.nombre
            itemView.findViewById<TextView>(R.id.textDireccion).text = gasolinera.direccion

            val textoPrecio = itemView.findViewById<TextView>(R.id.textPrecio)
            val precioFinal: Double
            val textoFinal: String

            if (tipoCombustible == "gasolina") {
                precioFinal = gasolinera.precioGasolina95
                textoFinal = String.format("Gasolina: %.2f €/L", precioFinal)
            } else if (tipoCombustible == "diésel" || tipoCombustible == "diesel") {
                precioFinal = gasolinera.precioDiesel
                textoFinal = String.format("Diésel: %.2f €/L", precioFinal)
            } else {
                textoFinal = String.format(
                    "Gasolina: %.2f €/L\nDiésel: %.2f €/L",
                    gasolinera.precioGasolina95,
                    gasolinera.precioDiesel
                )
                precioFinal = -1.0 // No se usará para guardar
            }

            textoPrecio.text = textoFinal

            // Mostrar distancia si se conoce ubicación
            if (ubicacionActual != null) {
                val userLoc = ubicacionActual!!
                val gasLoc = Location("").apply {
                    latitude = gasolinera.lat
                    longitude = gasolinera.lon
                }
                val distanciaMetros = userLoc.distanceTo(gasLoc)
                val distanciaKm = distanciaMetros / 1000.0

                val textDistancia = itemView.findViewById<TextView>(R.id.textDistancia)
                textDistancia.text = String.format("A %.1f km", distanciaKm)
                textDistancia.visibility = TextView.VISIBLE
            }







            itemView.findViewById<ImageButton>(R.id.btnVisitar).setOnClickListener {
                val uri = Uri.parse("geo:0,0?q=${gasolinera.lat},${gasolinera.lon}(${Uri.encode(gasolinera.nombre)})")
                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                    setPackage("com.google.android.apps.maps")
                }
                if (intent.resolveActivity(requireContext().packageManager) != null) {
                    startActivity(intent)
                }
            }

            itemView.setOnClickListener {
                if (precioFinal > 0) {
                    prefs.edit().putFloat("precio_litro", precioFinal.toFloat()).apply()
                }
                onSeleccion(gasolinera)
                dismiss()
            }

            contenedor.addView(itemView)
        }
    }

    private fun obtenerUbicacionYReordenar(onFinish: () -> Unit) {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(requireContext(), "Permiso de ubicación no concedido", Toast.LENGTH_SHORT).show()
            onFinish()
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                ubicacionActual = location
                val ordenadas = gasolineras.sortedBy {
                    val results = FloatArray(1)
                    Location.distanceBetween(location.latitude, location.longitude, it.lat, it.lon, results)
                    results[0]
                }
                mostrarGasolineras(ordenadas)
            } else {
                Toast.makeText(requireContext(), "Ubicación no disponible", Toast.LENGTH_SHORT).show()
            }
            onFinish()
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Error obteniendo la ubicación", Toast.LENGTH_SHORT).show()
            onFinish()
        }
    }
}
