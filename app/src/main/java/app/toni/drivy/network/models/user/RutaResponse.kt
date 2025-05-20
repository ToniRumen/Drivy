package app.toni.drivy.network.models.user

data class RutaResponse(
    val id: Long,
    val origen: String,
    val destino: String,
    val distanciaKm: Double,
    val costeEstimado: Double,
    val modoConduccion: String,
    val fecha: String
)
