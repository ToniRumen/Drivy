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
    private val rutas: List<RutaResponse>,                        // Lista de rutas a mostrar
    private val onLongClickEliminar: (RutaResponse) -> Unit       // Acción al mantener pulsado para eliminar
) : RecyclerView.Adapter<RutaAdapter.RutaViewHolder>() {

    // ViewHolder que representa cada item de ruta en el RecyclerView
    inner class RutaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val origenDestino: TextView = view.findViewById(R.id.textOrigenDestino)
        val datosRuta: TextView = view.findViewById(R.id.textDatosRuta)
        val fecha: TextView = view.findViewById(R.id.textFecha)
    }

    // Infla el layout de una ruta y crea el ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RutaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ruta, parent, false)
        return RutaViewHolder(view)
    }

    // Asigna los datos de una ruta al ViewHolder
    override fun onBindViewHolder(holder: RutaViewHolder, position: Int) {
        val ruta = rutas[position]
        val precioFormateado = String.format("%.2f", ruta.costeEstimado)

        // Muestra origen y destino como: "Madrid → Valencia"
        holder.origenDestino.text = "${ruta.origen} → ${ruta.destino}"

        // Muestra detalles como: "350 km · 23.80 € · Modo Eco"
        holder.datosRuta.text = "${ruta.distanciaKm} km · $precioFormateado € · Modo ${ruta.modoConduccion}"

        // Muestra la fecha en formato español amigable
        holder.fecha.text = formatearFecha(ruta.fecha)

        // Detecta pulsación larga para eliminar la ruta
        holder.itemView.setOnLongClickListener {
            onLongClickEliminar(ruta)
            true
        }
    }

    // Retorna el número total de rutas en la lista
    override fun getItemCount(): Int = rutas.size
}

/**
 * Convierte una fecha en formato ISO ("2023-11-08T15:30:00")
 * a ("8 nov 2023 · 15:30")
 */
fun formatearFecha(fechaIso: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date: Date = parser.parse(fechaIso)!!
        val formatter = SimpleDateFormat("d MMM yyyy · HH:mm", Locale("es", "ES"))
        formatter.format(date)
    } catch (e: Exception) {
        fechaIso // En caso de error, se devuelve el texto original
    }
}
