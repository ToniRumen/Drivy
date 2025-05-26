package app.toni.drivy.fragments.menu

import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class RutasFragment : Fragment() {

    private lateinit var recycler: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var textoSinRutas: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_rutas, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recycler = view.findViewById(R.id.recyclerRutas)
        progressBar = view.findViewById(R.id.progressHistorial)
        textoSinRutas = view.findViewById(R.id.textoSinRutas)
        recycler.layoutManager = LinearLayoutManager(requireContext())

        recargarRutas()
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
                        val rutas = response.body()!!
                        if (rutas.isEmpty()) {
                            textoSinRutas.visibility = View.VISIBLE
                            recycler.visibility = View.GONE
                        } else {
                            textoSinRutas.visibility = View.GONE
                            recycler.adapter = RutaAdapter(rutas) { rutaSeleccionada ->
                                val opciones = arrayOf("Cargar ruta", "Eliminar ruta")
                                AlertDialog.Builder(requireContext())
                                    .setTitle("Ruta: ${rutaSeleccionada.origen} - ${rutaSeleccionada.destino}")
                                    .setItems(opciones) { _, which ->
                                        when (which) {
                                            0 -> cargarRutaSeleccionada(rutaSeleccionada)
                                            1 -> mostrarDialogoEliminar(rutaSeleccionada.id)
                                        }
                                    }
                                    .show()
                            }

                            recycler.visibility = View.VISIBLE
                        }
                    } else {
                        textoSinRutas.visibility = View.VISIBLE
                        recycler.visibility = View.GONE
                        Toast.makeText(requireContext(), "Error al cargar rutas", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<RutaResponse>>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    textoSinRutas.visibility = View.VISIBLE
                    recycler.visibility = View.GONE
                    Toast.makeText(requireContext(), "Fallo de red: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun cargarRutaSeleccionada(ruta: RutaResponse) {
        val prefs = requireActivity().getSharedPreferences("app", 0)
        prefs.edit()
            .putString("ruta_texto", "${ruta.origen} - ${ruta.destino}")
            .putString("modo_conduccion", ruta.modoConduccion)
            .putFloat("precio_litro", prefs.getFloat("precio_litro", 1.65f)) // último o default
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

            // Volver a InicioFragment (ya cargado previamente con addToBackStack("inicio"))
            val pager = requireActivity().findViewById<ViewPager2>(R.id.viewPager)
            pager.currentItem = 1 // ← Esto te manda directo al fragmento central (InicioFragment)


        } else {
            Toast.makeText(requireContext(), "No se pudieron encontrar las ubicaciones", Toast.LENGTH_SHORT).show()
        }

        parentFragmentManager.setFragmentResult("ruta_cargada", Bundle())

        val pager = requireActivity().findViewById<ViewPager2>(R.id.viewPager)
        pager.currentItem = 1


    }

    private fun mostrarDialogoEliminar(id: Long) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar ruta")
            .setMessage("¿Seguro que quieres eliminar esta ruta?")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarRutaEnBackend(id)
            }
            .setNegativeButton("Cancelar", null)
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
