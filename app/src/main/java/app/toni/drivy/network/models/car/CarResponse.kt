package app.toni.drivy.network.models.car

import java.io.Serializable

data class CarResponse(
    val id: Long,
    val marca: String,
    val modelo: String,
    val anio: Int,
    val consumoMedio: Double,
    val tipoCombustible: String,
    val creadoPor: String
) : Serializable
