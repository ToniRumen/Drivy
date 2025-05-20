package app.toni.drivy.network.models.user

data class RutaRequest(
    val origen: String,
    val destino: String,
    val distanciaKm: Double,
    val costeEstimado: Double,
    val modoConduccion: String
)
