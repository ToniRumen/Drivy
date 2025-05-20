package app.toni.drivy.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.toni.drivy.R
import app.toni.drivy.network.models.user.RutaResponse
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RutaAdapter(
    private val rutas: List<RutaResponse>,
    private val onLongClickEliminar: (RutaResponse) -> Unit
) : RecyclerView.Adapter<RutaAdapter.RutaViewHolder>() {

    inner class RutaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val origenDestino: TextView = view.findViewById(R.id.textOrigenDestino)
        val datosRuta: TextView = view.findViewById(R.id.textDatosRuta)
        val fecha: TextView = view.findViewById(R.id.textFecha)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RutaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ruta, parent, false)
        return RutaViewHolder(view)
    }

    override fun onBindViewHolder(holder: RutaViewHolder, position: Int) {
        val ruta = rutas[position]
        val precioFormateado = String.format("%.2f", ruta.costeEstimado)
        holder.origenDestino.text = "${ruta.origen} → ${ruta.destino}"
        holder.datosRuta.text = "${ruta.distanciaKm} km · $precioFormateado € · Modo ${ruta.modoConduccion}"
        holder.fecha.text = formatearFecha(ruta.fecha)

        // Pulsación larga para eliminar
        holder.itemView.setOnLongClickListener {
            onLongClickEliminar(ruta)
            true
        }
    }

    override fun getItemCount(): Int = rutas.size
}

fun formatearFecha(fechaIso: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date: Date = parser.parse(fechaIso)!!
        val formatter = SimpleDateFormat("d MMM yyyy · HH:mm", Locale("es", "ES"))
        formatter.format(date)
    } catch (e: Exception) {
        fechaIso
    }
}
