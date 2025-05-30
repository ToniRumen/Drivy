package app.toni.drivy.fragments.coches

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import app.toni.drivy.R
import app.toni.drivy.adapters.CocheAdapter
import app.toni.drivy.network.models.car.CarResponse
import app.toni.drivy.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CochesComunidadFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private var cochesAdapter: CocheAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_coches, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()

        recyclerView = view.findViewById(R.id.recyclerCoches)
        progressBar = view.findViewById(R.id.progressCoches)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        cargarCochesComunidad()
    }

    override fun onResume() {
        super.onResume()
        cargarCochesComunidad() // 🔁 Refresca al volver a esta pestaña
    }

    private fun cargarCochesComunidad() {
        val prefs = requireActivity().getSharedPreferences("app", 0)
        val token = prefs.getString("jwt", null)

        if (token != null) {
            progressBar.visibility = View.VISIBLE
            RetrofitClient.authApi.getCochesComunidad("Bearer $token")
                .enqueue(object : Callback<List<CarResponse>> {
                    override fun onResponse(
                        call: Call<List<CarResponse>>,
                        response: Response<List<CarResponse>>
                    ) {
                        progressBar.visibility = View.GONE
                        if (response.isSuccessful && response.body() != null) {
                            val coches = response.body()!!
                            if (coches.isEmpty()) {
                                Toast.makeText(requireContext(), "Aún no hay coches compartidos", Toast.LENGTH_SHORT).show()
                            }

                            cochesAdapter = CocheAdapter(
                                coches,
                                mostrarCreador = true,
                                onCocheClick = { cocheSeleccionado ->
                                    prefs.edit()
                                        .putString("coche_nombre", "${cocheSeleccionado.marca} ${cocheSeleccionado.modelo}")
                                        .putFloat("coche_consumo", cocheSeleccionado.consumoMedio.toFloat())
                                        .putString("tipo_combustible", cocheSeleccionado.tipoCombustible)
                                        .putInt("coche_anio", cocheSeleccionado.anio)
                                        .apply()
                                    val activity = requireActivity() as? AppCompatActivity
                                    val viewPager = activity?.findViewById<ViewPager2>(R.id.viewPager)
                                    viewPager?.currentItem = 1
                                },
                                onLongClick = {
                                    Toast.makeText(requireContext(), "No puedes editar coches de la comunidad", Toast.LENGTH_SHORT).show()
                                }
                            )
                            recyclerView.adapter = cochesAdapter
                        } else {
                            Toast.makeText(requireContext(), "Error al cargar coches", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<List<CarResponse>>, t: Throwable) {
                        progressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), "Error de red: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                })
        } else {
            Toast.makeText(requireContext(), "Token no disponible", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
    }
}
