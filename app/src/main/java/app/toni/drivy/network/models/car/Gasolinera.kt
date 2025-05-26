package app.toni.drivy.network.models.car

data class Gasolinera(
    val nombre: String,
    val direccion: String,
    val horario: String,
    val precioGasolina95: Double,
    val precioGasolina98: Double,
    val precioDiesel: Double,
    val precioHibrido: Double,
    val precioElectrico: Double,
    val lat: Double,
    val lon: Double
)