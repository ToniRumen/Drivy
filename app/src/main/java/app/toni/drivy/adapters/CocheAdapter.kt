package app.toni.drivy.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.toni.drivy.R
import app.toni.drivy.network.models.car.CarResponse

class CocheAdapter(
    private val coches: List<CarResponse>,                 // Lista de coches a mostrar
    private val mostrarCreador: Boolean = true,            // Muestra el creador si es true
    private val onCocheClick: (CarResponse) -> Unit,       // Callback al hacer clic
    private val onLongClick: ((CarResponse) -> Unit)? = null // Callback al mantener pulsado (opcional)
) : RecyclerView.Adapter<CocheAdapter.CocheViewHolder>() {

    // ViewHolder para contener las referencias a los elementos de cada item
    inner class CocheViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.txtNombreCoche)
        val detalles: TextView = view.findViewById(R.id.txtDetallesCoche)
        val creadoPor: TextView = view.findViewById(R.id.txtCreadoPor)
    }

    // Infla el layout del coche (item_coche.xml) y crea un nuevo ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CocheViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_coche, parent, false)
        return CocheViewHolder(view)
    }

    // Asigna los datos de un coche al ViewHolder correspondiente
    override fun onBindViewHolder(holder: CocheViewHolder, position: Int) {
        val coche = coches[position]

        holder.nombre.text = "${coche.marca} ${coche.modelo}"
        holder.detalles.text = "${coche.tipoCombustible} - ${coche.consumoMedio} L/100km - ${coche.anio}"

        if (mostrarCreador) {
            holder.creadoPor.text = "Añadido por: ${coche.creadoPor}"
            holder.creadoPor.visibility = View.VISIBLE
        } else {
            holder.creadoPor.visibility = View.GONE
        }

        // Evento: clic simple sobre un coche
        holder.itemView.setOnClickListener {
            onCocheClick(coche)
        }

        // Evento: clic largo (si está definido)
        holder.itemView.setOnLongClickListener {
            onLongClick?.invoke(coche)
            true
        }
    }

    // Devuelve la cantidad total de ítems en la lista
    override fun getItemCount(): Int = coches.size
}
