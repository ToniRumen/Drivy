package app.toni.drivy.dialogs

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import app.toni.drivy.R
import app.toni.drivy.network.models.car.Gasolinera
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

private lateinit var locationPermissionLauncher: androidx.activity.result.ActivityResultLauncher<String>



class GasolineraDialogFragment(
    private val gasolineras: List<Gasolinera>,
    private val onSeleccion: (Gasolinera) -> Unit


) : DialogFragment() {

    private lateinit var locationPermissionLauncher: ActivityResultLauncher<String>
    private var onFinishPending: (() -> Unit)? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var prefs: android.content.SharedPreferences
    private lateinit var contenedor: LinearLayout

    private var ubicacionActual: Location? = null
    private lateinit var tipoCombustible: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                Toast.makeText(requireContext(), "Permiso concedido. Obteniendo ubicación...", Toast.LENGTH_SHORT).show()
                obtenerUbicacionYReordenar(onFinishPending ?: {})
            } else {
                Toast.makeText(requireContext(), "Permiso denegado. No se puede ordenar por cercanía.", Toast.LENGTH_SHORT).show()
                onFinishPending?.invoke()
            }
            onFinishPending = null
        }

    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_gasolineras, null)


        // Referencias de UI
        contenedor = view.findViewById(R.id.containerGasolineras)
        val btnActualizar = view.findViewById<Button>(R.id.btnActualizarGasolineras)
        val anim = AnimationUtils.loadAnimation(requireContext(), R.anim.boton_reflejo)

        prefs = requireContext().getSharedPreferences("app", 0)
        tipoCombustible = prefs.getString("tipo_combustible", null)?.lowercase() ?: "gasolina"
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Mostrar lista inicial
        mostrarGasolineras(gasolineras)
        obtenerUbicacionSinOrdenar() // Para mostrar distancias si se puede

        // Botón de actualizar (reordenar por cercanía)
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

    /**
     * Intenta obtener la ubicación una vez para mostrar las distancias,
     * sin reordenar la lista.
     */
    private fun obtenerUbicacionSinOrdenar() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) return

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                ubicacionActual = it
                mostrarGasolineras(gasolineras)
            }
        }
    }

    /**
     * Muestra la lista de gasolineras, con precios según el tipo de combustible
     * y la distancia si se conoce la ubicación actual.
     */
    private fun mostrarGasolineras(lista: List<Gasolinera>) {
        contenedor.removeAllViews()
        val inflater = requireActivity().layoutInflater

        lista.forEach { gasolinera ->
            val itemView = inflater.inflate(R.layout.item_gasolinera_card, contenedor, false)

            // Asignar datos de la gasolinera
            itemView.findViewById<TextView>(R.id.textNombre).text = gasolinera.nombre
            itemView.findViewById<TextView>(R.id.textDireccion).text = gasolinera.direccion

            val textoPrecio = itemView.findViewById<TextView>(R.id.textPrecio)
            val precioFinal: Double
            val textoFinal: String

            when (tipoCombustible) {
                "gasolina" -> {
                    precioFinal = gasolinera.precioGasolina95
                    textoFinal = "Gasolina: %.2f €/L".format(precioFinal)
                }
                "diésel", "diesel" -> {
                    precioFinal = gasolinera.precioDiesel
                    textoFinal = "Diésel: %.2f €/L".format(precioFinal)
                }
                else -> {
                    precioFinal = -1.0 // No guardamos
                    textoFinal = "Gasolina: %.2f €/L\nDiésel: %.2f €/L".format(
                        gasolinera.precioGasolina95,
                        gasolinera.precioDiesel
                    )
                }
            }

            textoPrecio.text = textoFinal

            // Mostrar distancia si hay ubicación disponible
            ubicacionActual?.let { userLoc ->
                val gasLoc = Location("").apply {
                    latitude = gasolinera.lat
                    longitude = gasolinera.lon
                }
                val distanciaKm = userLoc.distanceTo(gasLoc) / 1000.0
                itemView.findViewById<TextView>(R.id.textDistancia).apply {
                    text = "A %.1f km".format(distanciaKm)
                    visibility = TextView.VISIBLE
                }
            }

            // Botón para abrir en Google Maps
            itemView.findViewById<ImageButton>(R.id.btnVisitar).setOnClickListener {
                val uri = Uri.parse("geo:0,0?q=${gasolineras[0].lat},${gasolineras[0].lon}(${Uri.encode(gasolinera.nombre)})")
                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                    setPackage("com.google.android.apps.maps")
                }
                if (intent.resolveActivity(requireContext().packageManager) != null) {
                    startActivity(intent)
                }
            }

            // Selección del item (guardando precio si aplica)
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

    /**
     * Obtiene la ubicación actual y reordena la lista por cercanía.
     */
    fun obtenerUbicacionYReordenar(onFinish: () -> Unit = {}) {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            // Guardamos el callback para llamarlo más tarde si se concede
            this.onFinishPending = onFinish
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
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
