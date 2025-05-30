package app.toni.drivy.fragments.menu

import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import app.toni.drivy.R
import app.toni.drivy.adapters.RutaAdapter
import app.toni.drivy.network.RetrofitClient
import app.toni.drivy.network.models.user.RutaResponse
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class RutasFragment : Fragment() {

    private lateinit var recycler: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var textoSinRutas: TextView

    // CHIPS PARA FILTRO MODERNO
    private lateinit var layoutResumenGasto: LinearLayout
    private lateinit var textoResumenGasto: TextView
    private lateinit var chipGroupFiltro: ChipGroup
    private lateinit var chipSemana: Chip
    private lateinit var chipMes: Chip
    private lateinit var chipSiempre: Chip

    private var rutasOriginal: List<RutaResponse> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_rutas, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recycler = view.findViewById(R.id.recyclerRutas)
        progressBar = view.findViewById(R.id.progressHistorial)
        textoSinRutas = view.findViewById(R.id.textoSinRutas)
        recycler.layoutManager = LinearLayoutManager(requireContext())

        layoutResumenGasto = view.findViewById(R.id.layoutResumenGasto)
        textoResumenGasto = view.findViewById(R.id.textoResumenGasto)
        chipGroupFiltro = view.findViewById(R.id.chipGroupFiltro)
        chipSemana = view.findViewById(R.id.chipSemana)
        chipMes = view.findViewById(R.id.chipMes)
        chipSiempre = view.findViewById(R.id.chipSiempre)

        recargarRutas()

        chipGroupFiltro.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.chipSemana -> mostrarResumenFiltrado("semana")
                R.id.chipMes -> mostrarResumenFiltrado("mes")
                R.id.chipSiempre -> mostrarResumenFiltrado("siempre")
            }
        }
    }

    private fun recargarRutas() {
        val prefs = requireActivity().getSharedPreferences("app", 0)
        val token = prefs.getString("jwt", null) ?: return

        progressBar.visibility = View.VISIBLE

        RetrofitClient.authApi.getHistorialRutas("Bearer $token")
            .enqueue(object : Callback<List<RutaResponse>> {
                override fun onResponse(call: Call<List<RutaResponse>>, response: Response<List<RutaResponse>>) {
                    progressBar.visibility = View.GONE
                    if (response.isSuccessful && response.body() != null) {
                        rutasOriginal = response.body()!!
                        if (rutasOriginal.isEmpty()) {
                            textoSinRutas.visibility = View.VISIBLE
                            recycler.visibility = View.GONE
                            layoutResumenGasto.visibility = View.GONE
                        } else {
                            textoSinRutas.visibility = View.GONE
                            recycler.adapter = RutaAdapter(rutasOriginal) { rutaSeleccionada ->
                                val opciones = arrayOf(
                                    getString(R.string.opcion_cargar_ruta),
                                    getString(R.string.opcion_eliminar_ruta),
                                    getString(R.string.opcion_compartir_ruta)
                                )

                                AlertDialog.Builder(requireContext())
                                    .setTitle("Ruta: ${rutaSeleccionada.origen} - ${rutaSeleccionada.destino}")
                                    .setItems(opciones) { _, which ->
                                        when (which) {
                                            0 -> cargarRutaSeleccionada(rutaSeleccionada)
                                            1 -> mostrarDialogoEliminar(rutaSeleccionada.id)
                                            2 -> compartirRuta(rutaSeleccionada)

                                        }
                                    }
                                    .show()
                            }
                            recycler.visibility = View.VISIBLE
                            layoutResumenGasto.visibility = View.VISIBLE

                            when (chipGroupFiltro.checkedChipId) {
                                R.id.chipSemana -> mostrarResumenFiltrado("semana")
                                R.id.chipMes -> mostrarResumenFiltrado("mes")
                                R.id.chipSiempre -> mostrarResumenFiltrado("siempre")
                                else -> mostrarResumenFiltrado("semana")
                            }
                        }
                    } else {
                        textoSinRutas.visibility = View.VISIBLE
                        recycler.visibility = View.GONE
                        layoutResumenGasto.visibility = View.GONE
                        Toast.makeText(requireContext(), getString(R.string.error_cargar_rutas), Toast.LENGTH_SHORT).show()

                    }
                }

                private fun compartirRuta(ruta: RutaResponse) {
                    val mensaje = getString(
                        R.string.mensaje_compartir_ruta,
                        ruta.origen,
                        ruta.destino,
                        ruta.modoConduccion,
                        ruta.distanciaKm,
                        ruta.costeEstimado
                    )


                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, mensaje)
                        type = "text/plain"
                    }

                    startActivity(Intent.createChooser(intent, "Compartir ruta con..."))
                }


                override fun onFailure(call: Call<List<RutaResponse>>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    textoSinRutas.visibility = View.VISIBLE
                    recycler.visibility = View.GONE
                    layoutResumenGasto.visibility = View.GONE
                    Toast.makeText(requireContext(), "Fallo de red: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun mostrarResumenFiltrado(periodo: String) {
        val ahora = Date()
        val cal = Calendar.getInstance()
        val rutasFiltradas = when (periodo) {
            "semana" -> {
                cal.time = ahora
                cal.add(Calendar.DAY_OF_YEAR, -7)
                rutasOriginal.filter {
                    parseFechaISO(it.fecha)?.after(cal.time) == true
                }
            }
            "mes" -> {
                cal.time = ahora
                cal.add(Calendar.MONTH, -1)
                rutasOriginal.filter {
                    parseFechaISO(it.fecha)?.after(cal.time) == true
                }
            }
            "siempre" -> rutasOriginal
            else -> rutasOriginal
        }
        val totalGastado = rutasFiltradas.sumOf { it.costeEstimado }
        textoResumenGasto.text = getString(R.string.total_gastado, totalGastado)

    }

    private fun parseFechaISO(fechaIso: String): Date? {
        return try {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(fechaIso)
        } catch (e: Exception) {
            null
        }
    }

    private fun cargarRutaSeleccionada(ruta: RutaResponse) {
        val prefs = requireActivity().getSharedPreferences("app", 0)
        prefs.edit()
            .putString("ruta_texto", "${ruta.origen} - ${ruta.destino}")
            .putString("modo_conduccion", ruta.modoConduccion)
            .putFloat("precio_litro", prefs.getFloat("precio_litro", 1.65f))
            .apply()

        obtenerCoordenadasYCargar(ruta.origen, ruta.destino)
    }

    private fun obtenerCoordenadasYCargar(origen: String, destino: String) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val origenLatLng = geocoder.getFromLocationName(origen, 1)?.firstOrNull()?.let {
            LatLng(it.latitude, it.longitude)
        }
        val destinoLatLng = geocoder.getFromLocationName(destino, 1)?.firstOrNull()?.let {
            LatLng(it.latitude, it.longitude)
        }
        if (origenLatLng != null && destinoLatLng != null) {
            val prefs = requireActivity().getSharedPreferences("app", 0)
            prefs.edit()
                .putString("origen_lat", origenLatLng.latitude.toString())
                .putString("origen_lng", origenLatLng.longitude.toString())
                .putString("destino_lat", destinoLatLng.latitude.toString())
                .putString("destino_lng", destinoLatLng.longitude.toString())
                .apply()

            val pager = requireActivity().findViewById<ViewPager2>(R.id.viewPager)
            pager.currentItem = 1
        } else {
            Toast.makeText(requireContext(), "No se pudieron encontrar las ubicaciones", Toast.LENGTH_SHORT).show()
        }
        parentFragmentManager.setFragmentResult("ruta_cargada", Bundle())
        val pager = requireActivity().findViewById<ViewPager2>(R.id.viewPager)
        pager.currentItem = 1
    }

    private fun mostrarDialogoEliminar(id: Long) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.eliminar_ruta))
            .setMessage(getString(R.string.confirmar_eliminar_ruta))
            .setPositiveButton(getString(R.string.eliminar)) { _, _ ->
                eliminarRutaEnBackend(id)
            }
            .setNegativeButton(getString(R.string.cancelar), null)
            .show()
    }

    private fun eliminarRutaEnBackend(id: Long) {
        val prefs = requireActivity().getSharedPreferences("app", 0)
        val token = prefs.getString("jwt", null) ?: return

        progressBar.visibility = View.VISIBLE

        RetrofitClient.authApi.eliminarRuta("Bearer $token", id)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    progressBar.visibility = View.GONE
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Ruta eliminada", Toast.LENGTH_SHORT).show()
                        recargarRutas()
                    } else {
                        Toast.makeText(requireContext(), "No se pudo eliminar la ruta", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Error de red: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onResume() {
        super.onResume()
        recargarRutas()
    }
}
