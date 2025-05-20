package app.toni.drivy.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.toni.drivy.R
import app.toni.drivy.network.models.car.CarResponse

class CocheAdapter(
    private val coches: List<CarResponse>,
    private val mostrarCreador: Boolean = true,
    private val onCocheClick: (CarResponse) -> Unit,
    private val onLongClick: ((CarResponse) -> Unit)? = null

) : RecyclerView.Adapter<CocheAdapter.CocheViewHolder>() {



    inner class CocheViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.txtNombreCoche)
        val detalles: TextView = view.findViewById(R.id.txtDetallesCoche)
        val creadoPor: TextView = view.findViewById(R.id.txtCreadoPor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CocheViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_coche, parent, false)
        return CocheViewHolder(view)
    }

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

        holder.itemView.setOnClickListener {
            onCocheClick(coche) // 👈 Dispara callback
        }

        holder.itemView.setOnLongClickListener {
            onLongClick?.invoke(coche)
            true
        }

    }



    override fun getItemCount(): Int = coches.size
}
