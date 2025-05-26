package app.toni.drivy.fragments.menu

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import app.toni.drivy.network.models.car.EstacionServicio
import app.toni.drivy.network.models.car.Gasolinera
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import app.toni.drivy.R
import app.toni.drivy.dialogs.GasolineraDialogFragment
import app.toni.drivy.dialogs.ModoConduccionDialogFragment
import app.toni.drivy.dialogs.DialogSeleccionCiudad
import app.toni.drivy.network.RetrofitClient
import app.toni.drivy.network.models.user.RutaRequest
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InicioFragment : Fragment() {

    private lateinit var mapView: MapView
    private lateinit var barraConsumo: ProgressBar
    private lateinit var textoComentario: TextView
    private lateinit var textConsumo: TextView
    private lateinit var textNombreCoche: TextView
    private lateinit var textModoSeleccionado: TextView
    private lateinit var botonGuardarRuta: Button
    private var googleMap: GoogleMap? = null

    private var origenLatLng: LatLng? = null
    private var destinoLatLng: LatLng? = null
    private var ultimaDistanciaKm: Double = 0.0

    private val API_KEY = "AIzaSyDEyTgGFym-4Nci_cDiWOy-wzRPB2jJBU0"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_inicio, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()

        barraConsumo = view.findViewById(R.id.barraConsumo)
        textoComentario = view.findViewById(R.id.textoComentario)
        textConsumo = view.findViewById(R.id.textConsumo)
        textNombreCoche = view.findViewById(R.id.textCocheNombre)
        textModoSeleccionado = view.findViewById(R.id.textoModoSeleccionado)
        botonGuardarRuta = view.findViewById(R.id.btnGuardarRuta)
        botonGuardarRuta.visibility = View.GONE

        mapView = view.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        MapsInitializer.initialize(requireContext(), MapsInitializer.Renderer.LATEST) {}

        refrescarDatosCocheYRuta()

        mapView.getMapAsync {
            googleMap = it
            val prefs = requireActivity().getSharedPreferences("app", 0)
            origenLatLng = getLatLngFromPrefs(prefs, "origen")
            destinoLatLng = getLatLngFromPrefs(prefs, "destino")
            if (origenLatLng != null && destinoLatLng != null) {
                view.findViewById<TextView>(R.id.textRutaNombre).text = prefs.getString("ruta_texto", "Ruta")
                obtenerRutaReal(origenLatLng!!, destinoLatLng!!)
            }
        }

        // Cambiar modo conducción
        view.findViewById<LinearLayout>(R.id.tarjetaModoConduccion).setOnClickListener {
            ModoConduccionDialogFragment().show(parentFragmentManager, "ModoDialog")
        }

        // Seleccionar coche
        view.findViewById<LinearLayout>(R.id.tarjetaCoche).setOnClickListener {
            (requireActivity() as? AppCompatActivity)?.findViewById<ViewPager2>(R.id.viewPager)?.currentItem = 2
        }

        // Seleccionar ruta
        view.findViewById<LinearLayout>(R.id.tarjetaRuta).setOnClickListener {
            DialogSeleccionCiudad().show(parentFragmentManager, "DialogRuta")
        }

        // Escuchar resultado de seleccionar ruta
        parentFragmentManager.setFragmentResultListener("ruta_personalizada", viewLifecycleOwner) { _, bundle ->
            origenLatLng = bundle.getParcelable("origen_latlng")
            destinoLatLng = bundle.getParcelable("destino_latlng")
            val origenNombre = bundle.getString("origen_nombre")
            val destinoNombre = bundle.getString("destino_nombre")

            val prefs = requireActivity().getSharedPreferences("app", 0)
            prefs.edit()
                .putString("ruta_texto", "$origenNombre - $destinoNombre")
                .putString("origen_lat", origenLatLng?.latitude.toString())
                .putString("origen_lng", origenLatLng?.longitude.toString())
                .putString("destino_lat", destinoLatLng?.latitude.toString())
                .putString("destino_lng", destinoLatLng?.longitude.toString())
                .apply()

            view.findViewById<TextView>(R.id.textRutaNombre).text = "$origenNombre - $destinoNombre"
            if (origenLatLng != null && destinoLatLng != null) {
                obtenerRutaReal(origenLatLng!!, destinoLatLng!!)
            }
        }

        // Seleccionar gasolinera
        // Seleccionar gasolinera
        view.findViewById<LinearLayout>(R.id.tarjetaGasolinera).setOnClickListener {
            val prefs = requireActivity().getSharedPreferences("app", 0)
            val tipoCombustible = prefs.getString("tipo_combustible", "Gasolina") ?: "Gasolina"
            val lat = origenLatLng?.latitude ?: 40.42
            val lon = origenLatLng?.longitude ?: -3.70

            val progress = ProgressDialog.show(
                requireContext(),
                null,
                "Cargando gasolineras cercanas...",
                true,
                false
            )

            lifecycleScope.launch {
                try {
                    val response = withContext(Dispatchers.IO) {
                        app.toni.drivy.network.RetrofitClient.precioilApi.obtenerEstacionesCercanas(
                            latitud = lat,
                            longitud = lon,
                            radio = 10,
                            pagina = 1,
                            limite = 20
                        )
                    }
                    progress.dismiss()

                    if (response.isSuccessful && response.body() != null) {
                        val listaOriginal = response.body()!!.map { estacion ->
                            Gasolinera(
                                nombre = estacion.rotulo ?: "",
                                direccion = estacion.direccion ?: "",
                                horario = estacion.horario ?: "",
                                precioGasolina95 = estacion.gasolina95 ?: 0.0,
                                precioGasolina98 = estacion.gasolina98 ?: 0.0,
                                precioDiesel = estacion.diesel ?: 0.0,
                                precioHibrido = 0.0,
                                precioElectrico = 0.0,
                                lat = estacion.latitud ?: 0.0,
                                lon = estacion.longitud ?: 0.0
                            )
                        }

                        // --- DEBUG: Mira todos los precios que llegan ---
                        val preciosTodos = listaOriginal.map {
                            it.precioGasolina95 to it.precioGasolina98 to it.precioDiesel
                        }
                        Log.d("GASOLINERAS", "Precios de estaciones: $preciosTodos")

                        // Calcula la moda de precios
                        val preciosValidos = listaOriginal.mapNotNull { gasolinera ->
                            when (tipoCombustible.lowercase()) {
                                "gasolina", "gasolina 95" -> gasolinera.precioGasolina95.takeIf { it > 0 }
                                "gasolina 98" -> gasolinera.precioGasolina98.takeIf { it > 0 }
                                "diésel", "diesel" -> gasolinera.precioDiesel.takeIf { it > 0 }
                                "híbrido" -> gasolinera.precioHibrido.takeIf { it > 0 }
                                "eléctrico" -> gasolinera.precioElectrico.takeIf { it > 0 }
                                else -> gasolinera.precioGasolina95.takeIf { it > 0 }
                            }
                        }

                        Log.d("GASOLINERAS", "Precios válidos para $tipoCombustible: $preciosValidos")

                        val conteo = preciosValidos
                            .groupingBy { Math.round(it * 100).toInt() }
                            .eachCount()
                        val entryMax = conteo.entries.maxByOrNull { entry -> entry.value }
                        // Si no hay moda pero hay algún precio válido, usamos el mínimo. Si no, 1.65
                        val precioPorDefecto = when {
                            entryMax != null -> entryMax.key / 100.0
                            preciosValidos.isNotEmpty() -> preciosValidos.minOrNull() ?: 1.65
                            else -> 1.65
                        }

                        GasolineraDialogFragment(
                            gasolineras = listaOriginal,
                            tipoCombustible = tipoCombustible,
                            precioEstimado = precioPorDefecto,
                            onActualizar = { /* Aquí puedes refrescar si quieres */ }
                        ) { gasolinera, precioRealUsado ->
                            actualizarTarjetaGasolinera(gasolinera, precioRealUsado)
                        }.show(parentFragmentManager, "GasolineraDialog")
                    } else {
                        Toast.makeText(requireContext(), "No se pudieron cargar gasolineras", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    progress.dismiss()
                    Toast.makeText(requireContext(), "Error: " + (e.localizedMessage ?: e.toString()), Toast.LENGTH_LONG).show()
                    Log.e("Gasolineras", "Error cargando", e)
                }
            }
        }




        // Botón guardar ruta
        botonGuardarRuta.setOnClickListener {
            confirmarYGuardarRuta()
        }

        parentFragmentManager.setFragmentResultListener("modo_seleccionado", viewLifecycleOwner) { _, _ ->
            refrescarDatosCocheYRuta()
        }
        parentFragmentManager.setFragmentResultListener("ruta_cargada", viewLifecycleOwner) { _, _ ->
            recargarVista()
        }
    }

    // Esta función solo la usas si tu backend expone recarga manual
    private fun recargarGasolinerasDesdeBackend(lat: Double, lon: Double, tipoCombustible: String) {
        val progress = ProgressDialog.show(
            requireContext(),
            null,
            "Actualizando gasolineras...",
            true,
            false
        )

        lifecycleScope.launch {
            try {
                // Si tienes endpoint para recargar, usa aquí RetrofitClient.gasolineraApi.recargarGasolineras()
                val listaOriginal = withContext(Dispatchers.IO) {
                    RetrofitClient.gasolineraApi.getGasolinerasCercanas(lat, lon)
                } as List<app.toni.drivy.network.models.car.Gasolinera>
                val preciosValidos = listaOriginal.mapNotNull {
                    when (tipoCombustible.lowercase()) {
                        "gasolina", "gasolina 95" -> it.precioGasolina95.takeIf { p -> p > 0 }
                        "gasolina 98" -> it.precioGasolina98.takeIf { p -> p > 0 }
                        "diésel", "diesel" -> it.precioDiesel.takeIf { p -> p > 0 }
                        "híbrido" -> it.precioHibrido.takeIf { p -> p > 0 }
                        "eléctrico" -> it.precioElectrico.takeIf { p -> p > 0 }
                        else -> it.precioGasolina95.takeIf { p -> p > 0 }
                    }
                }

                val precioPorDefecto = preciosValidos
                    .groupingBy { Math.round(it * 100).toInt() }
                    .eachCount()
                    .maxByOrNull { it.value }
                    ?.key?.let { it / 100.0 } ?: 1.65

                progress.dismiss()
                GasolineraDialogFragment(
                    gasolineras = listaOriginal,
                    tipoCombustible = tipoCombustible,
                    precioEstimado = precioPorDefecto,
                    onActualizar = { recargarGasolinerasDesdeBackend(lat, lon, tipoCombustible) }
                ) { gasolinera, precio ->
                    actualizarTarjetaGasolinera(gasolinera, precio)
                }.show(parentFragmentManager, "GasolineraDialog")
            } catch (e: Exception) {
                progress.dismiss()
                Toast.makeText(requireContext(), "Error al actualizar gasolineras", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun recargarVista() {
        val prefs = requireActivity().getSharedPreferences("app", 0)
        val nombreCoche = prefs.getString("coche_nombre", "Sin selección")
        val consumoGuardado = prefs.getFloat("coche_consumo", 0.0f)
        val origenLat = prefs.getString("origen_lat", null)?.toDoubleOrNull()
        val origenLng = prefs.getString("origen_lng", null)?.toDoubleOrNull()
        val destinoLat = prefs.getString("destino_lat", null)?.toDoubleOrNull()
        val destinoLng = prefs.getString("destino_lng", null)?.toDoubleOrNull()
        val rutaTexto = prefs.getString("ruta_texto", "Ruta no definida")

        textNombreCoche.text = nombreCoche
        textConsumo.text = String.format("%.1f L/100km", consumoGuardado)
        view?.findViewById<TextView>(R.id.textRutaNombre)?.text = rutaTexto

        if (origenLat != null && origenLng != null && destinoLat != null && destinoLng != null) {
            val origenLatLng = LatLng(origenLat, origenLng)
            val destinoLatLng = LatLng(destinoLat, destinoLng)
            obtenerRutaReal(origenLatLng, destinoLatLng)
        }
        mostrarModoSeleccionado()
    }

    private fun mostrarModoSeleccionado() {
        val prefs = requireActivity().getSharedPreferences("app", 0)
        val modo = prefs.getString("modo_conduccion", null) ?: "Normal"
        prefs.edit().putString("modo_conduccion", modo).apply()
        when (modo.lowercase()) {
            "eco" -> textModoSeleccionado.setTextColor(resources.getColor(android.R.color.holo_blue_light, null))
            "normal" -> textModoSeleccionado.setTextColor(resources.getColor(android.R.color.white, null))
            "race" -> textModoSeleccionado.setTextColor(resources.getColor(android.R.color.holo_red_light, null))
        }
        textModoSeleccionado.text = "Modo ${modo.replaceFirstChar { it.uppercase() }}"
        if (origenLatLng != null && destinoLatLng != null) {
            obtenerRutaReal(origenLatLng!!, destinoLatLng!!)
        }
    }

    private fun refrescarDatosCocheYRuta() {
        val prefs = requireActivity().getSharedPreferences("app", 0)
        val nombreCoche = prefs.getString("coche_nombre", "Sin selección")
        val consumoGuardado = prefs.getFloat("coche_consumo", 0f)
        val tipoCombustible = prefs.getString("tipo_combustible", "Gasolina") ?: "Gasolina"
        val modo = prefs.getString("modo_conduccion", "Normal") ?: "Normal"

        textNombreCoche.text = nombreCoche
        val factor = when (modo.lowercase()) {
            "eco" -> 0.8f
            "race" -> 1.2f
            else -> 1.0f
        }
        textConsumo.text = String.format("%.1f L/100km", consumoGuardado)
        textModoSeleccionado.text = "Modo ${modo.replaceFirstChar { it.uppercase() }}"

        when (modo.lowercase()) {
            "eco" -> textModoSeleccionado.setTextColor(resources.getColor(android.R.color.holo_blue_light, null))
            "race" -> textModoSeleccionado.setTextColor(resources.getColor(android.R.color.holo_red_light, null))
            else -> textModoSeleccionado.setTextColor(resources.getColor(android.R.color.white, null))
        }

        origenLatLng = getLatLngFromPrefs(prefs, "origen")
        destinoLatLng = getLatLngFromPrefs(prefs, "destino")

        if (origenLatLng != null && destinoLatLng != null) {
            obtenerRutaReal(origenLatLng!!, destinoLatLng!!)
            view?.findViewById<TextView>(R.id.textRutaNombre)?.text = prefs.getString("ruta_texto", "Ruta")
        }
    }

    private fun getLatLngFromPrefs(prefs: SharedPreferences, keyPrefix: String): LatLng? {
        val lat = prefs.getString("${keyPrefix}_lat", null)?.toDoubleOrNull()
        val lng = prefs.getString("${keyPrefix}_lng", null)?.toDoubleOrNull()
        return if (lat != null && lng != null) LatLng(lat, lng) else null
    }

    private fun actualizarTarjetaGasolinera(g: Gasolinera, precio: Double) {
        view?.findViewById<TextView>(R.id.textoGasolineraNombre)?.text = g.nombre
        view?.findViewById<TextView>(R.id.textoGasolineraPrecio)?.text = String.format("%.2f €/L", precio)
        requireActivity().getSharedPreferences("app", 0).edit().putFloat("precio_litro", precio.toFloat()).apply()
        if (origenLatLng != null && destinoLatLng != null) obtenerRutaReal(origenLatLng!!, destinoLatLng!!)
    }

    private fun obtenerRutaReal(origen: LatLng, destino: LatLng) {
        val url = "https://maps.googleapis.com/maps/api/directions/json?origin=${origen.latitude},${origen.longitude}&destination=${destino.latitude},${destino.longitude}&mode=driving&key=$API_KEY"
        Thread {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                val response = BufferedReader(InputStreamReader(connection.inputStream)).readText()
                val json = JSONObject(response)
                val routesArray = json.getJSONArray("routes")
                if (routesArray.length() == 0) return@Thread

                val route = routesArray.getJSONObject(0)
                val points = route.getJSONObject("overview_polyline").getString("points")
                val path = decodePolyline(points)
                val distanceKm = Math.round(
                    route.getJSONArray("legs")
                        .getJSONObject(0)
                        .getJSONObject("distance")
                        .getInt("value") / 1000.0
                ).toInt()

                requireActivity().runOnUiThread {
                    googleMap?.clear()
                    googleMap?.addMarker(MarkerOptions().position(origen).title("Origen"))
                    googleMap?.addMarker(MarkerOptions().position(destino).title("Destino"))
                    googleMap?.addPolyline(PolylineOptions().addAll(path).width(6f).color(resources.getColor(android.R.color.holo_red_light, null)))
                    val bounds = LatLngBounds.Builder().apply { path.forEach { include(it) } }.build()
                    googleMap?.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 120))

                    ultimaDistanciaKm = distanceKm.toDouble()
                    calcularYMostrarCoste(distanceKm.toDouble())
                    actualizarBarraYComentario(requireContext().getSharedPreferences("app", 0).getFloat("coche_consumo", 0f), distanceKm.toDouble())

                    botonGuardarRuta.visibility = View.VISIBLE
                    botonGuardarRuta.text = "Guardar esta ruta"
                    botonGuardarRuta.isEnabled = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly = mutableListOf<LatLng>()
        var index = 0
        var lat = 0
        var lng = 0
        while (index < encoded.length) {
            var b: Int; var shift = 0; var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift); shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) result.inv() shr 1 else result shr 1
            lat += dlat
            shift = 0; result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift); shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) result.inv() shr 1 else result shr 1
            lng += dlng
            poly.add(LatLng(lat / 1E5, lng / 1E5))
        }
        return poly
    }

    private fun calcularYMostrarCoste(distanciaKm: Double) {
        val prefs = requireActivity().getSharedPreferences("app", 0)
        val consumo = prefs.getFloat("coche_consumo", 0f)
        val tipo = prefs.getString("tipo_combustible", "Gasolina") ?: "Gasolina"
        val modo = prefs.getString("modo_conduccion", "Normal") ?: "Normal"
        val precio = prefs.getFloat("precio_litro", -1f).takeIf { it > 0 } ?: when (tipo.lowercase()) {
            "gasolina" -> 1.65
            "diésel" -> 1.55
            "híbrido" -> 1.60
            "eléctrico" -> 0.20
            else -> 1.60
        }
        val factor = when (modo.lowercase()) {
            "eco" -> 0.9
            "race" -> 1.2
            else -> 1.0
        }
        val consumoAjustado = consumo * factor
        val coste = (consumoAjustado / 100.0) * distanciaKm * precio.toDouble()
        view?.findViewById<TextView>(R.id.textoCosteEstimado)?.text = String.format("Coste estimado: %.2f €", coste)
    }

    private fun actualizarBarraYComentario(consumo: Float, distanciaKm: Double) {
        val prefs = requireActivity().getSharedPreferences("app", 0)
        val modo = prefs.getString("modo_conduccion", "Normal") ?: "Normal"
        val factor = when (modo.lowercase()) {
            "eco" -> 0.9f
            "race" -> 1.2f
            else -> 1.0f
        }

        barraConsumo.max = 100
        val ajustado = consumo * factor
        val consumoMaximo = 15.0f
        val porcentaje = when {
            ajustado <= 0f -> 0
            ajustado >= consumoMaximo -> 100
            else -> ((ajustado / consumoMaximo) * 100).toInt()
        }

        barraConsumo.progress = porcentaje
        textConsumo.text = String.format("%.1f L/100km", ajustado)

        val color = when {
            ajustado <= 5f -> R.color.verde_consumo
            ajustado <= 9f -> R.color.amarillo_consumo
            else -> R.color.rojo_consumo
        }
        barraConsumo.progressTintList = ContextCompat.getColorStateList(requireContext(), color)

        textoComentario.text = when {
            ajustado <= 3.5f -> "¡Que disfrute para el bolsillo!"
            ajustado <= 5.5f -> "Buen consumo, buen viaje."
            ajustado <= 7.5f -> "Consumo adecuado."
            ajustado <= 11f -> "La cartera lo notará."
            ajustado <= 13f -> "Consumo alto, haz que valga la pena"
            else -> "¡Mirar el consumo es para mediocres, di que sí!"
        }
    }

    private fun confirmarYGuardarRuta() {
        AlertDialog.Builder(requireContext())
            .setTitle("Guardar ruta")
            .setMessage("¿Deseas guardar esta ruta en tu historial?")
            .setPositiveButton("Sí") { _, _ ->
                if (origenLatLng != null && destinoLatLng != null) {
                    guardarRutaEnBackend(origenLatLng!!, destinoLatLng!!, ultimaDistanciaKm)
                    botonGuardarRuta.isEnabled = false
                    botonGuardarRuta.text = "Ruta guardada"
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun guardarRutaEnBackend(origen: LatLng, destino: LatLng, distanciaKm: Double) {
        val prefs = requireActivity().getSharedPreferences("app", 0)
        val token = prefs.getString("jwt", null) ?: return
        val rutaTexto = prefs.getString("ruta_texto", "") ?: ""
        val origenNombre = rutaTexto.split(" - ").getOrNull(0) ?: "?"
        val destinoNombre = rutaTexto.split(" - ").getOrNull(1) ?: "?"
        val modo = prefs.getString("modo_conduccion", "Normal") ?: "Normal"
        val consumo = prefs.getFloat("coche_consumo", 0f)
        val tipo = prefs.getString("tipo_combustible", "gasolina") ?: "gasolina"
        val precio = prefs.getFloat("precio_litro", -1f).takeIf { it > 0 } ?: when (tipo.lowercase()) {
            "gasolina" -> 1.65
            "diésel" -> 1.55
            "híbrido" -> 1.60
            "eléctrico" -> 0.20
            else -> 1.60
        }
        val factor = when (modo.lowercase()) {
            "eco" -> 0.9
            "race" -> 1.2
            else -> 1.0
        }
        val litros = (consumo * factor / 100.0) * distanciaKm
        val coste = litros * precio.toDouble()

        val request = RutaRequest(origenNombre, destinoNombre, distanciaKm, coste, modo)
        RetrofitClient.authApi.guardarRuta("Bearer $token", request)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Ruta guardada correctamente", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Error al guardar ruta", Toast.LENGTH_SHORT).show()
                        botonGuardarRuta.isEnabled = true
                        botonGuardarRuta.text = "Guardar esta ruta"
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(requireContext(), "Fallo al guardar: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                    botonGuardarRuta.isEnabled = true
                    botonGuardarRuta.text = "Guardar esta ruta"
                }
            })
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        refrescarDatosCocheYRuta()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}
