package app.toni.drivy.network.models.car

data class CocheUpdateRequest(
    val marca: String,
    val modelo: String,
    val anio: Int,
    val consumoMedio: Double,
    val tipoCombustible: String
)
