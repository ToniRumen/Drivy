package app.toni.drivy.fragments.menu

import android.app.AlertDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import app.toni.drivy.R
import app.toni.drivy.dialogs.*
import app.toni.drivy.network.RetrofitClient
import app.toni.drivy.network.models.car.Gasolinera
import app.toni.drivy.network.models.user.RutaRequest
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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

        // Refrescar al entrar
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

        // Botón de cambiar modo conducción
        view.findViewById<LinearLayout>(R.id.tarjetaModoConduccion).setOnClickListener {
            ModoConduccionDialogFragment().show(parentFragmentManager, "ModoDialog")
        }

        // Botón de seleccionar coche
        view.findViewById<LinearLayout>(R.id.tarjetaCoche).setOnClickListener {
            (requireActivity() as? AppCompatActivity)?.findViewById<ViewPager2>(R.id.viewPager)?.currentItem = 2
        }

        // Botón seleccionar ruta
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

        // Botón seleccionar gasolinera
        view.findViewById<LinearLayout>(R.id.tarjetaGasolinera).setOnClickListener {
            val lista = cargarGasolinerasLocales()
            GasolineraDialogFragment(lista) {
                actualizarTarjetaGasolinera(it)
            }.show(parentFragmentManager, "GasolineraDialog")
        }

        // Botón guardar ruta
        botonGuardarRuta.setOnClickListener {
            confirmarYGuardarRuta()
        }

        // Listener para modo de conducción actualizado
        parentFragmentManager.setFragmentResultListener("modo_seleccionado", viewLifecycleOwner) { _, _ ->
            refrescarDatosCocheYRuta()
        }
    }

    private fun refrescarDatosCocheYRuta() {
        // Refresca datos de coche y recalcula coste/ruta
        val prefs = requireActivity().getSharedPreferences("app", 0)
        val nombreCoche = prefs.getString("coche_nombre", "Sin selección")
        val consumoGuardado = prefs.getFloat("coche_consumo", 0f)
        val tipoCombustible = prefs.getString("tipo_combustible", "Gasolina") ?: "Gasolina"
        val modo = prefs.getString("modo_conduccion", "Normal") ?: "Normal"

        textNombreCoche.text = nombreCoche

        // ⬇️ Ajusta el consumo según el modo
        val factor = when (modo.lowercase()) {
            "eco" -> 0.9f
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

        // Recalcular si hay ruta y coche
        origenLatLng = getLatLngFromPrefs(prefs, "origen")
        destinoLatLng = getLatLngFromPrefs(prefs, "destino")

        if (origenLatLng != null && destinoLatLng != null) {
            obtenerRutaReal(origenLatLng!!, destinoLatLng!!)
            view?.findViewById<TextView>(R.id.textRutaNombre)?.text = prefs.getString("ruta_texto", "Ruta")
        }
    }

    private fun cargarGasolinerasLocales(): List<Gasolinera> {
        return try {
            val json = requireContext().assets.open("gasolineras.json")
                .bufferedReader()
                .use { it.readText() }

            val type = object : TypeToken<List<Gasolinera>>() {}.type
            Gson().fromJson(json, type)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun getLatLngFromPrefs(prefs: SharedPreferences, keyPrefix: String): LatLng? {
        val lat = prefs.getString("${keyPrefix}_lat", null)?.toDoubleOrNull()
        val lng = prefs.getString("${keyPrefix}_lng", null)?.toDoubleOrNull()
        return if (lat != null && lng != null) LatLng(lat, lng) else null
    }

    private fun actualizarTarjetaGasolinera(g: Gasolinera) {
        view?.findViewById<TextView>(R.id.textoGasolineraNombre)?.text = g.nombre
        view?.findViewById<TextView>(R.id.textoGasolineraPrecio)?.text = String.format("%.2f €/L", g.precioGasolina95)
        requireActivity().getSharedPreferences("app", 0).edit().putFloat("precio_litro", g.precioGasolina95.toFloat()).apply()
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
                val distanceKm = route.getJSONArray("legs").getJSONObject(0).getJSONObject("distance").getInt("value") / 1000.0

                requireActivity().runOnUiThread {
                    googleMap?.clear()
                    googleMap?.addMarker(MarkerOptions().position(origen).title("Origen"))
                    googleMap?.addMarker(MarkerOptions().position(destino).title("Destino"))
                    googleMap?.addPolyline(PolylineOptions().addAll(path).width(6f).color(resources.getColor(android.R.color.holo_red_light, null)))
                    val bounds = LatLngBounds.Builder().apply { path.forEach { include(it) } }.build()
                    googleMap?.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 120))

                    ultimaDistanciaKm = distanceKm
                    calcularYMostrarCoste(distanceKm)
                    actualizarBarraYComentario(requireContext().getSharedPreferences("app", 0).getFloat("coche_consumo", 0f), distanceKm)

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
        val modo = requireActivity().getSharedPreferences("app", 0).getString("modo_conduccion", "Normal") ?: "Normal"
        val factor = when (modo.lowercase()) {
            "eco" -> 0.9f
            "race" -> 1.2f
            else -> 1.0f
        }
        val ajustado = consumo * factor
        val porcentaje = ((ajustado * 10f).toInt()).coerceIn(0, 100)
        barraConsumo.progress = porcentaje
        textConsumo.text = String.format("%.1f L/100km", ajustado)
        textoComentario.text = when {
            ajustado <= 3.5f -> "¡Que disfute para el bolsillo!"
            ajustado <= 5.5f -> "Buen consumo, buen viaje."
            ajustado <= 7.5f -> "Consumo normal."
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
        RetrofitClient.instance.guardarRuta("Bearer $token", request)
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
