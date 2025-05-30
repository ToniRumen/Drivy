package app.toni.drivy.fragments.coches

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import app.toni.drivy.R
import app.toni.drivy.adapters.CocheAdapter
import app.toni.drivy.dialogs.AnadirCocheDialogFragment
import app.toni.drivy.dialogs.EditarCocheDialogFragment
import app.toni.drivy.network.models.car.CarResponse
import app.toni.drivy.network.RetrofitClient
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MisCochesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var textoSinCoches: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_coches, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()

        recyclerView = view.findViewById(R.id.recyclerCoches)
        progressBar = view.findViewById(R.id.progressCoches)
        textoSinCoches = view.findViewById(R.id.textSinCoches)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val fab = view.findViewById<FloatingActionButton>(R.id.fabAddCar)
        fab.visibility = View.VISIBLE
        fab.setOnClickListener {
            AnadirCocheDialogFragment {
                recargarMisCoches()
            }.show(parentFragmentManager, "DialogAddCar")
        }

        recargarMisCoches()
    }

    private fun recargarMisCoches() {
        val prefs = requireActivity().getSharedPreferences("app", 0)
        val token = prefs.getString("jwt", null) ?: return

        progressBar.visibility = View.VISIBLE
        RetrofitClient.authApi.getMisCoches("Bearer $token")
            .enqueue(object : Callback<List<CarResponse>> {
                override fun onResponse(
                    call: Call<List<CarResponse>>,
                    response: Response<List<CarResponse>>
                ) {
                    progressBar.visibility = View.GONE
                    if (response.isSuccessful && response.body() != null) {
                        val coches = response.body()!!
                        textoSinCoches.visibility = if (coches.isEmpty()) View.VISIBLE else View.GONE

                        recyclerView.adapter = CocheAdapter(
                            coches,
                            mostrarCreador = false,
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
                            onLongClick = { coche ->
                                val opciones = arrayOf("Editar", "Eliminar")
                                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                                    .setTitle("Opciones para ${coche.marca} ${coche.modelo}")
                                    .setItems(opciones) { _, which ->
                                        when (which) {
                                            0 -> { // Editar
                                                EditarCocheDialogFragment.newInstance(coche) {
                                                    recargarMisCoches()
                                                }.show(parentFragmentManager, "EditarCocheDialog")
                                            }
                                            1 -> { // Eliminar
                                                val prefs = requireActivity().getSharedPreferences("app", 0)
                                                val token = prefs.getString("jwt", null) ?: return@setItems
                                                RetrofitClient.authApi.eliminarCoche(coche.id.toInt(), "Bearer $token")
                                                    .enqueue(object : Callback<Void> {
                                                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                                            if (response.isSuccessful) {
                                                                Toast.makeText(requireContext(), "Coche eliminado", Toast.LENGTH_SHORT).show()
                                                                recargarMisCoches()
                                                            } else {
                                                                Toast.makeText(requireContext(), "Error al eliminar", Toast.LENGTH_SHORT).show()
                                                            }
                                                        }

                                                        override fun onFailure(call: Call<Void>, t: Throwable) {
                                                            Toast.makeText(requireContext(), "Error de red: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                                                        }
                                                    })
                                            }
                                        }
                                    }
                                    .show()
                            }

                        )
                    } else {
                        Toast.makeText(requireContext(), "Error al cargar coches", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<CarResponse>>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Error de red: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
    }
}
