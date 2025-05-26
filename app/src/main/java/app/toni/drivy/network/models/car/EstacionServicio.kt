package app.toni.drivy.network.models.car

import com.google.gson.annotations.SerializedName

data class EstacionServicio(
    @SerializedName("nombreEstacion") val rotulo: String?,
    @SerializedName("direccion") val direccion: String?,
    @SerializedName("horario") val horario: String?,
    @SerializedName("Gasolina95") val gasolina95: Double?,
    @SerializedName("Gasolina98") val gasolina98: Double?,
    @SerializedName("Diesel") val diesel: Double?,
    @SerializedName("DieselPremium") val dieselPremium: Double?,
    @SerializedName("latitud") val latitud: Double?,
    @SerializedName("longitud") val longitud: Double?
)
