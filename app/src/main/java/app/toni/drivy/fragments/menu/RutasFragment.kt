package app.toni.drivy.fragments.menu

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
import app.toni.drivy.R
import app.toni.drivy.adapters.RutaAdapter
import app.toni.drivy.network.RetrofitClient
import app.toni.drivy.network.models.user.RutaResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

        RetrofitClient.instance.getHistorialRutas("Bearer $token")
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
                                mostrarDialogoEliminar(rutaSeleccionada.id)
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

        RetrofitClient.instance.eliminarRuta("Bearer $token", id)
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
